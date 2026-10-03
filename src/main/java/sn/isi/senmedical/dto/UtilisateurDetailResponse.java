// UtilisateurDetailResponse.java
// Profil complet d'un utilisateur, pour la vue detail/edition admin
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UtilisateurDetailResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String dateCreation;
    private Boolean actif;
}