// UtilisateurModifierRequest.java
// Donnees modifiables par l'admin sur un compte utilisateur
package sn.isi.senmedical.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtilisateurModifierRequest {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
}