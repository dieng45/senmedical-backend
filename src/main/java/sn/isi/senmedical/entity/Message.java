// Message.java
// Un message individuel echange dans une consultation (patient ou chatbot)
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
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Expediteur expediteur;

    @Column(name = "date_envoi", updatable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();
}