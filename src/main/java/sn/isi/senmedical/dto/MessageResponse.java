// MessageResponse.java
// Represente un message dans le detail d'une consultation
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponse {
    private String contenu;
    private String expediteur; // PATIENT ou CHATBOT
    private String dateEnvoi;
}