// LocalisationRequest.java
// Coordonnees GPS envoyees par le navigateur du patient en cas d'urgence
package sn.isi.senmedical.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalisationRequest {
    private double latitude;
    private double longitude;
}