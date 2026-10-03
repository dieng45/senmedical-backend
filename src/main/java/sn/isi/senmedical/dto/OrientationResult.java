// OrientationResult.java
// Represente le resultat structure extrait de la reponse texte de Gemini
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrientationResult {
    private String specialite;
    private String gravite; // FAIBLE, MOYEN ou ELEVE
    private String recommandation;
}