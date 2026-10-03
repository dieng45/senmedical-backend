// UrgenceRepository.java
package sn.isi.senmedical.repository;

import sn.isi.senmedical.entity.Urgence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrgenceRepository extends JpaRepository<Urgence, Long> {
    Optional<Urgence> findByConsultationId(Long consultationId);
}