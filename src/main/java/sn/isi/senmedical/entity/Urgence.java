// Urgence.java
// Declenchee automatiquement quand le niveau de gravite est ELEVE
// Contient la localisation du patient et l'hopital propose via Google Maps API
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
@Table(name = "urgence")
public class Urgence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    private Double latitude;

    private Double longitude;

    // Nom de l'hopital le plus proche retourne par Google Maps API
    @Column(name = "hopital_propose")
    private String hopitalPropose;

    @Column(name = "telephone_hopital")
    private String telephoneHopital;

    @Column(name = "date_declenchement", updatable = false)
    private LocalDateTime dateDeclenchement = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutUrgence statut = StatutUrgence.EN_ATTENTE;
}