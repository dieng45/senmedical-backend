// ProfilResponse.java
// Informations du profil patient renvoyees a l'affichage de la page "Mon profil"
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfilResponse {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String dateNaissance; // null si non renseignee
    private String sexe;
    private String groupeSanguin;
    private String antecedents;
    private String allergies;
}