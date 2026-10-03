// DocumentRequest.java
// Donnees envoyees par l'admin pour ajouter un document medical a la base de connaissances
package sn.isi.senmedical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;

    private String source;
    private String categorie;
}