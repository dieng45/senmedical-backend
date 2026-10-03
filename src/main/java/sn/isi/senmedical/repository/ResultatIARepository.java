// ResultatIARepository.java
// Acces aux resultats d'orientation IA
package sn.isi.senmedical.repository;

import sn.isi.senmedical.entity.ResultatIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultatIARepository extends JpaRepository<ResultatIA, Long> {
    // Recupere tous les resultats d'une consultation, dans l'ordre chronologique d'insertion
    // (chaque resultat correspond a un echange message-patient + reponse-bot, dans le meme ordre)
    List<ResultatIA> findByConsultationIdOrderByIdAsc(Long consultationId);
}