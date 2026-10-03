// RegisterRequest.java
// Donnees envoyees par le patient lors de l'inscription (formulaire complet, cf. cas d'utilisation "S'inscrire")
package sn.isi.senmedical.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    private String telephone;
    private LocalDate dateNaissance;
    private String sexe;
    private String groupeSanguin;
    private String antecedents;
    private String allergies;
}