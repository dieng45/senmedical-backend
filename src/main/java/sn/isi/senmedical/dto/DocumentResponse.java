// DocumentResponse.java
// Represente un document medical dans les reponses de l'API
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String titre;
    private String source;
    private String categorie;
    private String dateAjout;
}