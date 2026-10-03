// MessageRepository.java
// Acces aux donnees de la table message
package sn.isi.senmedical.repository;

import sn.isi.senmedical.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Recupere tous les messages d'une consultation, dans l'ordre chronologique d'insertion
    List<Message> findByConsultationIdOrderByIdAsc(Long consultationId);
}