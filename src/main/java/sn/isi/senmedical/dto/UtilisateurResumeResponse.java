// UtilisateurResumeResponse.java
// Resume d'un utilisateur patient, pour la liste admin "Utilisateurs"
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UtilisateurResumeResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String dateCreation;
}