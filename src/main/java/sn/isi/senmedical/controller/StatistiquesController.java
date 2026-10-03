// StatistiquesController.java
// Fournit les chiffres cles et les donnees des graphiques pour le tableau de bord admin et la page Statistiques
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.*;
import sn.isi.senmedical.entity.*;
import sn.isi.senmedical.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/statistiques")
public class StatistiquesController {

    private final UtilisateurRepository utilisateurRepository;
    private final ConsultationRepository consultationRepository;
    private final DocumentMedicalRepository documentMedicalRepository;
    private final ResultatIARepository resultatIARepository;

    public StatistiquesController(UtilisateurRepository utilisateurRepository,
                                  ConsultationRepository consultationRepository,
                                  DocumentMedicalRepository documentMedicalRepository,
                                  ResultatIARepository resultatIARepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.consultationRepository = consultationRepository;
        this.documentMedicalRepository = documentMedicalRepository;
        this.resultatIARepository = resultatIARepository;
    }

    @GetMapping
    public ResponseEntity<?> obtenir() {

        List<Patient> patients = utilisateurRepository.findAll().stream()
                .filter(u -> u instanceof Patient)
                .map(u -> (Patient) u)
                .toList();
        long totalPatients = patients.size();

        List<Consultation> consultations = consultationRepository.findAll();
        long totalConsultations = consultations.size();

        List<DocumentMedical> documents = documentMedicalRepository.findAll();
        long totalDocuments = documents.size();

        List<ResultatIA> resultats = resultatIARepository.findAll();
        long casFaible = resultats.stream().filter(r -> r.getNiveauGravite() == NiveauGravite.FAIBLE).count();
        long casMoyen = resultats.stream().filter(r -> r.getNiveauGravite() == NiveauGravite.MOYEN).count();
        long casEleve = resultats.stream().filter(r -> r.getNiveauGravite() == NiveauGravite.ELEVE).count();
        long totalEvaluations = casFaible + casMoyen + casEleve;

        // Evolution des consultations sur les 7 derniers jours
        List<PointEvolutionResponse> evolution = new ArrayList<>();
        DateTimeFormatter formatJour = DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
        for (int i = 6; i >= 0; i--) {
            LocalDate jour = LocalDate.now().minusDays(i);
            long nombre = consultations.stream()
                    .filter(c -> c.getDateDebut().toLocalDate().equals(jour))
                    .count();
            evolution.add(new PointEvolutionResponse(jour.format(formatJour), nombre));
        }

        // Activite recente : dernieres inscriptions + derniers documents + dernieres urgences, fusionnees et triees
        List<ActiviteResponse> activites = new ArrayList<>();

        patients.stream()
                .sorted(Comparator.comparing(Patient::getDateCreation).reversed())
                .limit(3)
                .forEach(p -> activites.add(new ActiviteResponse(
                        "UTILISATEUR", "Nouvel utilisateur inscrit",
                        p.getPrenom() + " " + p.getNom() + " (" + p.getEmail() + ")",
                        p.getDateCreation()
                )));

        documents.stream()
                .sorted(Comparator.comparing(DocumentMedical::getDateAjout).reversed())
                .limit(3)
                .forEach(d -> activites.add(new ActiviteResponse(
                        "DOCUMENT", "Document ajouté", d.getTitre(), d.getDateAjout()
                )));

        resultats.stream()
                .filter(r -> r.getNiveauGravite() == NiveauGravite.ELEVE)
                .sorted(Comparator.comparing(ResultatIA::getDateGeneration).reversed())
                .limit(3)
                .forEach(r -> activites.add(new ActiviteResponse(
                        "URGENCE", "Consultation urgente détectée",
                        r.getSpecialiteRecommandee() != null ? r.getSpecialiteRecommandee() : "Cas à risque élevé",
                        r.getDateGeneration()
                )));

        List<ActiviteResponse> activiteRecente = activites.stream()
                .sorted(Comparator.comparing(ActiviteResponse::getDate).reversed())
                .limit(6)
                .toList();

        // Repartition des documents par categorie (pour la page Statistiques)
        Map<String, Long> documentsParCategorie = documents.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getCategorie() != null ? d.getCategorie() : "Non classé",
                        Collectors.counting()
                ));

        // Top specialites recommandees par MediBot IA
        Map<String, Long> specialites = resultats.stream()
                .filter(r -> r.getSpecialiteRecommandee() != null && !r.getSpecialiteRecommandee().isBlank())
                .collect(Collectors.groupingBy(ResultatIA::getSpecialiteRecommandee, Collectors.counting()));

        return ResponseEntity.ok(new StatistiquesResponse(
                totalPatients, totalConsultations, totalDocuments, totalEvaluations,
                casFaible, casMoyen, casEleve,
                evolution, activiteRecente,
                documentsParCategorie, specialites
        ));
    }
}