// StatistiquesResponse.java
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class StatistiquesResponse {
    private long totalPatients;
    private long totalConsultations;
    private long totalDocuments;
    private long totalEvaluationsGravite;

    private long repartitionFaible;
    private long repartitionModere;
    private long repartitionUrgent;

    private List<PointEvolutionResponse> evolutionConsultations;
    private List<ActiviteResponse> activiteRecente;

    // nouveau : pour la page Statistiques detaillee
    private Map<String, Long> documentsParCategorie;
    private Map<String, Long> specialitesLesPlusRecommandees;
}