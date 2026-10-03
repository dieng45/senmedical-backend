// ConsultationDetailResponse.java
// Detail complet d'une consultation : tous les messages + tous les resultats d'orientation
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ConsultationDetailResponse {
    private Long id;
    private String dateDebut;
    private String statut;
    private List<MessageResponse> messages;
    private List<ResultatResponse> resultats; // resultats[i] correspond a l'echange messages[2i]/messages[2i+1]
}