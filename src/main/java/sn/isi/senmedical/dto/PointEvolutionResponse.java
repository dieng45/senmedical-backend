// PointEvolutionResponse.java
// Un point du graphique d'evolution des consultations (nombre de consultations pour un jour donne)
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointEvolutionResponse {
    private String jour;   // ex : "22 sept."
    private long nombre;
}