// UrgenceService.java
// Cherche l'hopital le plus proche via Overpass API (donnees OpenStreetMap, gratuites, sans cle)
package sn.isi.senmedical.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import sn.isi.senmedical.dto.HopitalResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class UrgenceService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HopitalResponse trouverHopitalLePlusProche(double latitude, double longitude) {
        List<HopitalResponse> hopitaux = rechercherHopitaux(latitude, longitude, 5000);

        // si rien dans un rayon de 5 km, on elargit a 20 km (utile en zone peu couverte)
        if (hopitaux.isEmpty()) {
            hopitaux = rechercherHopitaux(latitude, longitude, 20000);
        }

        return hopitaux.stream()
                .min(Comparator.comparingDouble(HopitalResponse::getDistanceKm))
                .orElse(null);
    }

    private List<HopitalResponse> rechercherHopitaux(double latitude, double longitude, int rayonMetres) {
        String requeteOverpass = String.format(
                "[out:json];node[\"amenity\"=\"hospital\"](around:%d,%s,%s);out body 15;",
                rayonMetres, latitude, longitude
        );

        List<HopitalResponse> resultats = new ArrayList<>();

        try {
            HttpRequest requete = HttpRequest.newBuilder()
                    .uri(URI.create("https://overpass-api.de/api/interpreter"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "data=" + URLEncoder.encode(requeteOverpass, StandardCharsets.UTF_8)))
                    .build();

            HttpResponse<String> reponse = client.send(requete, HttpResponse.BodyHandlers.ofString());
            JsonNode racine = objectMapper.readTree(reponse.body());
            JsonNode elements = racine.get("elements");

            if (elements != null) {
                for (JsonNode element : elements) {
                    if (!element.has("lat") || !element.has("lon")) continue;

                    JsonNode tags = element.get("tags");
                    String nom = (tags != null && tags.has("name")) ? tags.get("name").asText() : "Hôpital (nom non renseigné)";
                    String telephone = null;
                    if (tags != null) {
                        if (tags.has("phone")) telephone = tags.get("phone").asText();
                        else if (tags.has("contact:phone")) telephone = tags.get("contact:phone").asText();
                    }

                    double latHopital = element.get("lat").asDouble();
                    double lonHopital = element.get("lon").asDouble();
                    double distance = calculerDistanceKm(latitude, longitude, latHopital, lonHopital);

                    resultats.add(new HopitalResponse(nom, latHopital, lonHopital, telephone, distance));
                }
            }
        } catch (Exception e) {
            // en cas d'echec reseau, on renvoie une liste vide plutot que de planter la requete
        }

        return resultats;
    }

    // Formule de Haversine : distance a vol d'oiseau entre 2 points GPS, en kilometres
    private double calculerDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double rayonTerre = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return rayonTerre * c;
    }
}