// Utilisateur.java
// Classe mere abstraite contenant les champs communs a Patient et Administrateur
// Strategie JOINED : chaque sous-classe a sa propre table, liee par cle etrangere sur l'id
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
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    // Le mot de passe sera toujours stocke sous forme hachee (BCrypt), jamais en clair
    @Column(nullable = false)
    private String motDePasse;

    private String telephone;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    // Utilise par Spring Security pour determiner les droits d'acces (PATIENT ou ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // false = compte desactive par l'admin, l'utilisateur ne peut plus se connecter
    @Column(nullable = false)
    private Boolean actif = true;
}