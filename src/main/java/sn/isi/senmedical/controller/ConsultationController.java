// ConsultationController.java
// Orchestre les cas d'utilisation "Decrire ses symptomes", "Obtenir une orientation",
// l'historique des consultations et leur suppression
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.ConsultationDetailResponse;
import sn.isi.senmedical.dto.ConsultationResponse;
import sn.isi.senmedical.dto.ConsultationResumeResponse;
import sn.isi.senmedical.dto.MessageRequest;
import sn.isi.senmedical.dto.MessageResponse;
import sn.isi.senmedical.dto.OrientationResult;
import sn.isi.senmedical.dto.ResultatResponse;
import sn.isi.senmedical.entity.*;
import sn.isi.senmedical.repository.*;
import sn.isi.senmedical.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationRepository consultationRepository;
    private final MessageRepository messageRepository;
    private final ResultatIARepository resultatIARepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ChatbotService chatbotService;

    public ConsultationController(ConsultationRepository consultationRepository,
                                  MessageRepository messageRepository,
                                  ResultatIARepository resultatIARepository,
                                  UtilisateurRepository utilisateurRepository,
                                  ChatbotService chatbotService) {
        this.consultationRepository = consultationRepository;
        this.messageRepository = messageRepository;
        this.resultatIARepository = resultatIARepository;
        this.utilisateurRepository = utilisateurRepository;
        this.chatbotService = chatbotService;
    }

    // POST /api/consultations/demarrer : cree une nouvelle consultation pour le patient connecte
    @PostMapping("/demarrer")
    public ResponseEntity<?> demarrerConsultation(Authentication authentication) {
        Patient patient = recupererPatientConnecte(authentication);

        Consultation consultation = new Consultation();
        consultation.setPatient(patient);
        consultation.setStatut(StatutConsultation.EN_COURS);
        consultationRepository.save(consultation);

        return ResponseEntity.ok().body("Consultation demarree avec l'id : " + consultation.getId());
    }

    // GET /api/consultations/historique : liste des consultations du patient connecte, les plus recentes d'abord
    @GetMapping("/historique")
    public ResponseEntity<?> getHistorique(Authentication authentication) {
        Patient patient = recupererPatientConnecte(authentication);

        List<Consultation> consultations = consultationRepository.findByPatientIdOrderByDateDebutDesc(patient.getId());

        List<ConsultationResumeResponse> reponse = consultations.stream()
                .map(consultation -> {
                    List<ResultatIA> resultats = resultatIARepository.findByConsultationIdOrderByIdAsc(consultation.getId());
                    String specialite = resultats.isEmpty() ? null : resultats.get(resultats.size() - 1).getSpecialiteRecommandee();
                    String gravite = resultats.isEmpty() ? null : resultats.get(resultats.size() - 1).getNiveauGravite().name();

                    return new ConsultationResumeResponse(
                            consultation.getId(),
                            consultation.getDateDebut().toString(),
                            consultation.getStatut().name(),
                            specialite,
                            gravite
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(reponse);
    }

    // POST /api/consultations/{id}/messages : le patient decrit ses symptomes, le chatbot repond
    @PostMapping("/{id}/messages")
    public ResponseEntity<?> envoyerMessage(@PathVariable Long id,
                                            @Valid @RequestBody MessageRequest request,
                                            Authentication authentication) {

        Patient patient = recupererPatientConnecte(authentication);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation introuvable"));

        if (!consultation.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Acces refuse a cette consultation");
        }

        Message messagePatient = new Message();
        messagePatient.setConsultation(consultation);
        messagePatient.setContenu(request.getContenu());
        messagePatient.setExpediteur(Expediteur.PATIENT);
        messageRepository.save(messagePatient);

        String reponseGemini = chatbotService.genererOrientation(
                request.getContenu(),
                patient.getAntecedents(),
                patient.getAllergies()
        );
        OrientationResult orientation = chatbotService.parserReponse(reponseGemini);

        Message messageChatbot = new Message();
        messageChatbot.setConsultation(consultation);
        messageChatbot.setContenu(orientation.getRecommandation());
        messageChatbot.setExpediteur(Expediteur.CHATBOT);
        messageRepository.save(messageChatbot);

        ResultatIA resultatIA = new ResultatIA();
        resultatIA.setConsultation(consultation);
        resultatIA.setSpecialiteRecommandee(orientation.getSpecialite());
        resultatIA.setNiveauGravite(NiveauGravite.valueOf(orientation.getGravite()));
        resultatIA.setRecommandation(orientation.getRecommandation());
        resultatIARepository.save(resultatIA);

        boolean urgenceDeclenchee = orientation.getGravite().equals("ELEVE");

        return ResponseEntity.ok(new ConsultationResponse(
                consultation.getId(),
                orientation.getRecommandation(),
                orientation.getSpecialite(),
                orientation.getGravite(),
                urgenceDeclenchee
        ));
    }

    // GET /api/consultations/{id} : detail complet d'une consultation (messages + resultats)
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id, Authentication authentication) {
        Patient patient = recupererPatientConnecte(authentication);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation introuvable"));

        if (!consultation.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Acces refuse a cette consultation");
        }

        List<Message> messages = messageRepository.findByConsultationIdOrderByIdAsc(id);
        List<ResultatIA> resultats = resultatIARepository.findByConsultationIdOrderByIdAsc(id);

        List<MessageResponse> messagesReponse = messages.stream()
                .map(m -> new MessageResponse(m.getContenu(), m.getExpediteur().name(), m.getDateEnvoi().toString()))
                .collect(Collectors.toList());

        List<ResultatResponse> resultatsReponse = resultats.stream()
                .map(r -> new ResultatResponse(r.getSpecialiteRecommandee(), r.getNiveauGravite().name(), r.getRecommandation()))
                .collect(Collectors.toList());

        ConsultationDetailResponse reponse = new ConsultationDetailResponse(
                consultation.getId(),
                consultation.getDateDebut().toString(),
                consultation.getStatut().name(),
                messagesReponse,
                resultatsReponse
        );

        return ResponseEntity.ok(reponse);
    }

    // DELETE /api/consultations/{id} : supprime une consultation du patient connecte
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerConsultation(@PathVariable Long id, Authentication authentication) {
        Patient patient = recupererPatientConnecte(authentication);

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation introuvable"));

        if (!consultation.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Acces refuse a cette consultation");
        }

        // Grace au cascade = CascadeType.ALL sur messages/resultatsIA/urgence dans Consultation.java,
        // supprimer la consultation supprime automatiquement tout ce qui lui est lie
        consultationRepository.delete(consultation);

        return ResponseEntity.ok("Consultation supprimee avec succes");
    }

    // Recupere le patient actuellement connecte a partir du contexte de securite (email dans le token JWT)
    private Patient recupererPatientConnecte(Authentication authentication) {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!(utilisateur instanceof Patient patient)) {
            throw new RuntimeException("Seuls les patients peuvent utiliser le chatbot");
        }
        return patient;
    }
}