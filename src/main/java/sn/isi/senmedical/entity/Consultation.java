// Consultation.java
// Represente une session d'echange entre un patient et le chatbot medical
// Contient les messages, les resultats d'orientation IA (un par message) et une eventuelle urgence
package sn.isi.senmedical.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "consultation")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "date_debut", updatable = false)
    private LocalDateTime dateDebut = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConsultation statut = StatutConsultation.EN_COURS;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    // Une consultation peut avoir plusieurs resultats d'orientation IA (un par echange de messages)
    // ATTENTION : c'est bien une List ici, pas un champ singulier - ne jamais remettre "private ResultatIA resultatIA;"
    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultatIA> resultatsIA = new ArrayList<>();

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Urgence urgence;
}