// ProfilUpdateRequest.java
// Donnees modifiables par le patient depuis la page "Mon profil"
package sn.isi.senmedical.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilUpdateRequest {
    private String nom;
    private String prenom;
    private String telephone;
    private String dateNaissance;
    private String sexe;
    private String groupeSanguin;
    private String antecedents;
    private String allergies;
}