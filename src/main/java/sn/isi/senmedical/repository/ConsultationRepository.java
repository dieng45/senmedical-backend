// ConsultationRepository.java
package sn.isi.senmedical.repository;

import sn.isi.senmedical.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByPatientIdOrderByDateDebutDesc(Long patientId);
}