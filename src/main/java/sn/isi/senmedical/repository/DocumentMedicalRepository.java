// DocumentMedicalRepository.java
// Acces aux documents medicaux utilises comme base de connaissances pour le RAG
package sn.isi.senmedical.repository;

import sn.isi.senmedical.entity.DocumentMedical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, Long> {
}