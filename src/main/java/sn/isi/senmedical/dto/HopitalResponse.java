// HopitalResponse.java
// Hopital trouve le plus proche du patient via OpenStreetMap (Overpass API)
package sn.isi.senmedical.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HopitalResponse {
    private String nom;
    private double latitude;
    private double longitude;
    private String telephone; // peut etre null si non renseigne dans OpenStreetMap
    private double distanceKm;
}