// Administrateur.java
// Herite de Utilisateur, represente le gestionnaire de la plateforme
package sn.isi.senmedical.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "administrateur")
@PrimaryKeyJoinColumn(name = "id")
public class Administrateur extends Utilisateur {

    private String fonction; // ex : "Super Admin", "Gestionnaire"
}