// MessageRequest.java
// Le message envoye par le patient au chatbot
package sn.isi.senmedical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {

    @NotBlank(message = "Le message ne peut pas etre vide")
    private String contenu;
}