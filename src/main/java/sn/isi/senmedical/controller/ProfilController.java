// ProfilController.java
// Expose les routes pour consulter et modifier le profil du patient connecte
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.ProfilResponse;
import sn.isi.senmedical.dto.ProfilUpdateRequest;
import sn.isi.senmedical.entity.Patient;
import sn.isi.senmedical.entity.Utilisateur;
import sn.isi.senmedical.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/profil")
public class ProfilController {

    private final UtilisateurRepository utilisateurRepository;

    public ProfilController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // GET /api/profil : renvoie les informations du patient connecte
    @GetMapping
    public ResponseEntity<?> getProfil(Authentication authentication) {
        Patient patient = recupererPatientConnecte(authentication);

        ProfilResponse reponse = new ProfilResponse(
                patient.getNom(),
                patient.getPrenom(),
                patient.getEmail(),
                patient.getTelephone(),
                patient.getDateNaissance() != null ? patient.getDateNaissance().toString() : null,
                patient.getSexe(),
                patient.getGroupeSanguin(),
                patient.getAntecedents(),
                patient.getAllergies()
        );

        return ResponseEntity.ok(reponse);
    }

    // PUT /api/profil : met a jour les informations du patient connecte
    @PutMapping
    public ResponseEntity<?> mettreAJourProfil(@RequestBody ProfilUpdateRequest request, Authentication authentication) {
        Patient patient = recupererPatientConnecte(authentication);

        if (request.getNom() != null) patient.setNom(request.getNom());
        if (request.getPrenom() != null) patient.setPrenom(request.getPrenom());
        if (request.getTelephone() != null) patient.setTelephone(request.getTelephone());
        if (request.getDateNaissance() != null && !request.getDateNaissance().isBlank()) {
            patient.setDateNaissance(LocalDate.parse(request.getDateNaissance()));
        }
        if (request.getSexe() != null) patient.setSexe(request.getSexe());
        if (request.getGroupeSanguin() != null) patient.setGroupeSanguin(request.getGroupeSanguin());
        if (request.getAntecedents() != null) patient.setAntecedents(request.getAntecedents());
        if (request.getAllergies() != null) patient.setAllergies(request.getAllergies());

        utilisateurRepository.save(patient);

        return ResponseEntity.ok("Profil mis a jour avec succes");
    }

    // Recupere le patient actuellement connecte a partir du contexte de securite (email dans le token JWT)
    private Patient recupererPatientConnecte(Authentication authentication) {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!(utilisateur instanceof Patient patient)) {
            throw new RuntimeException("Seuls les patients ont un profil de ce type");
        }
        return patient;
    }
}