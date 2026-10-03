// ConsultationResponse.java
// Ce que l'API renvoie apres l'analyse du chatbot : le message + le resultat d'orientation
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConsultationResponse {
    private Long consultationId;
    private String reponseChatbot;
    private String specialiteRecommandee;
    private String niveauGravite;
    private boolean urgenceDeclenchee;
}