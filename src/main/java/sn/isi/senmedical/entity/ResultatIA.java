// ResultatIA.java
// Resultat de l'orientation medicale genere par l'API Gemini via LangChain4j (RAG)
// Sans diagnostic medical : uniquement une orientation vers un specialiste + niveau de gravite
// Une consultation peut avoir plusieurs resultats (un par message envoye par le patient)
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
@Table(name = "resultat_ia")
public class ResultatIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IMPORTANT : @ManyToOne ici, pas @OneToOne - doit correspondre a List<ResultatIA> dans Consultation.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(name = "specialite_recommandee")
    private String specialiteRecommandee;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_gravite", nullable = false)
    private NiveauGravite niveauGravite;

    @Column(columnDefinition = "TEXT")
    private String recommandation;

    @Column(name = "date_generation", updatable = false)
    private LocalDateTime dateGeneration = LocalDateTime.now();
}