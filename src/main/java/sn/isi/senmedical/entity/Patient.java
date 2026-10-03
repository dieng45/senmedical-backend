// Patient.java
// Herite de Utilisateur, ajoute les informations medicales necessaires a l'orientation IA
package sn.isi.senmedical.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "patient")
@PrimaryKeyJoinColumn(name = "id") // relie cette table a utilisateur via la meme cle primaire
public class Patient extends Utilisateur {

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    private String sexe; // "M" ou "F"

    @Column(name = "groupe_sanguin")
    private String groupeSanguin;

    @Column(columnDefinition = "TEXT")
    private String antecedents;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    // Un patient possede plusieurs consultations (relation OneToMany, cf. entite Consultation a venir)
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultations = new ArrayList<>();
}