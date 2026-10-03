// DocumentMedical.java
// Document de la base de connaissances utilisee par le RAG (LangChain4j + pgvector)
// Note : l'embedding vectoriel lui-meme est gere separement par le PgVectorEmbeddingStore
// de LangChain4j, pas directement via cette entite JPA (le type "vector" n'est pas mappe par Hibernate)
package sn.isi.senmedical.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_medical")
public class DocumentMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    // D'ou provient le document (ex : "OMS", "Ministere de la Sante")
    private String source;

    // Ex : "Cardiologie", "Pediatrie", "Urgences"
    private String categorie;

    @Column(name = "date_ajout", updatable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();
}