// UrgenceController.java
// Recoit la position GPS du patient en cas d'urgence, trouve l'hopital le plus proche et l'enregistre
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.HopitalResponse;
import sn.isi.senmedical.dto.LocalisationRequest;
import sn.isi.senmedical.entity.Consultation;
import sn.isi.senmedical.entity.Urgence;
import sn.isi.senmedical.repository.ConsultationRepository;
import sn.isi.senmedical.repository.UrgenceRepository;
import sn.isi.senmedical.service.UrgenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class UrgenceController {

    private final ConsultationRepository consultationRepository;
    private final UrgenceRepository urgenceRepository;
    private final UrgenceService urgenceService;

    public UrgenceController(ConsultationRepository consultationRepository,
                             UrgenceRepository urgenceRepository,
                             UrgenceService urgenceService) {
        this.consultationRepository = consultationRepository;
        this.urgenceRepository = urgenceRepository;
        this.urgenceService = urgenceService;
    }

    // POST /api/consultations/{id}/urgence/localiser : cherche l'hopital le plus proche et enregistre l'urgence
    @PostMapping("/{id}/urgence/localiser")
    public ResponseEntity<?> localiser(@PathVariable Long id, @Valid @RequestBody LocalisationRequest request) {

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation introuvable"));

        HopitalResponse hopital = urgenceService.trouverHopitalLePlusProche(
                request.getLatitude(), request.getLongitude());

        Urgence urgence = urgenceRepository.findByConsultationId(id).orElse(new Urgence());
        urgence.setConsultation(consultation);
        urgence.setLatitude(request.getLatitude());
        urgence.setLongitude(request.getLongitude());

        if (hopital != null) {
            urgence.setHopitalPropose(hopital.getNom());
            urgence.setTelephoneHopital(hopital.getTelephone());
        }

        urgenceRepository.save(urgence);

        return ResponseEntity.ok(hopital);
    }
}