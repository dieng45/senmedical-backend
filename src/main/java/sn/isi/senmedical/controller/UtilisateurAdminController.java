// UtilisateurAdminController.java
// Gestion des comptes patients par l'administrateur : liste, recherche,
// detail, modification, activation/desactivation, suppression
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.UtilisateurResumeResponse;
import sn.isi.senmedical.dto.UtilisateurDetailResponse;
import sn.isi.senmedical.dto.UtilisateurModifierRequest;
import sn.isi.senmedical.entity.Patient;
import sn.isi.senmedical.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/utilisateurs")
public class UtilisateurAdminController {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurAdminController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // GET /api/admin/utilisateurs?recherche=xxx : liste (ou recherche par nom/prenom/email) des patients
    @GetMapping
    public ResponseEntity<?> lister(@RequestParam(required = false) String recherche) {
        List<UtilisateurResumeResponse> reponse = utilisateurRepository.findAll().stream()
                .filter(u -> u instanceof Patient) // exclut les comptes administrateurs
                .filter(u -> recherche == null || recherche.isBlank()
                        || u.getNom().toLowerCase().contains(recherche.toLowerCase())
                        || u.getPrenom().toLowerCase().contains(recherche.toLowerCase())
                        || u.getEmail().toLowerCase().contains(recherche.toLowerCase()))
                .map(u -> new UtilisateurResumeResponse(
                        u.getId(), u.getNom(), u.getPrenom(), u.getEmail(),
                        u.getTelephone(), u.getDateCreation().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reponse);
    }

    // GET /api/admin/utilisateurs/{id} : detail complet d'un utilisateur
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .map(u -> ResponseEntity.ok(new UtilisateurDetailResponse(
                        u.getId(), u.getNom(), u.getPrenom(), u.getEmail(),
                        u.getTelephone(), u.getDateCreation().toString(), u.getActif()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/admin/utilisateurs/{id} : modification des infos par l'admin
    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(@PathVariable Long id, @RequestBody UtilisateurModifierRequest requete) {
        return utilisateurRepository.findById(id)
                .map(u -> {
                    u.setNom(requete.getNom());
                    u.setPrenom(requete.getPrenom());
                    u.setEmail(requete.getEmail());
                    u.setTelephone(requete.getTelephone());
                    utilisateurRepository.save(u);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH /api/admin/utilisateurs/{id}/statut : bascule actif/desactive
    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> basculerStatut(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .map(u -> {
                    u.setActif(!u.getActif());
                    utilisateurRepository.save(u);
                    return ResponseEntity.ok(u.getActif());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/admin/utilisateurs/{id} : suppression definitive du compte
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(@PathVariable Long id) {
        if (!utilisateurRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        utilisateurRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}