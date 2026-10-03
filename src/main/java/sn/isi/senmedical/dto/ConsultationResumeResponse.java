// ConsultationResumeResponse.java
// Resume d'une consultation pour l'affichage dans la liste "Mes consultations"
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConsultationResumeResponse {
    private Long id;
    private String dateDebut;
    private String statut;
    private String specialiteRecommandee; // null si aucune orientation n'a encore ete generee
    private String niveauGravite;         // null si aucune orientation n'a encore ete generee
}