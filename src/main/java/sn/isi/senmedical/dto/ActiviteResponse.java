// ActiviteResponse.java
// Une ligne de l'activite recente affichee sur le tableau de bord
// type : "UTILISATEUR" | "DOCUMENT" | "URGENCE"
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActiviteResponse {
    private String type;
    private String titre;
    private String sousTitre;
    private LocalDateTime date;
}