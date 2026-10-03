// ResultatResponse.java
// Represente un resultat d'orientation dans le detail d'une consultation
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultatResponse {
    private String specialiteRecommandee;
    private String niveauGravite;
    private String recommandation;
}