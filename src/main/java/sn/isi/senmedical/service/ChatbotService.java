// ChatbotService.java
// Coeur du chatbot medical : recherche RAG dans les documents medicaux + generation de l'orientation via Gemini
// Respecte le sujet du memoire : oriente le patient sans jamais poser de diagnostic medical
package sn.isi.senmedical.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;
import sn.isi.senmedical.dto.OrientationResult;

import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public ChatbotService(ChatLanguageModel chatModel,
                          EmbeddingModel embeddingModel,
                          EmbeddingStore<TextSegment> embeddingStore) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    // Cherche les documents medicaux les plus pertinents par rapport aux symptomes decrits (RAG)
    private String rechercherContexteMedical(String symptomes) {
        Embedding embeddingSymptomes = embeddingModel.embed(symptomes).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingSymptomes)
                .maxResults(3)
                .minScore(0.5)
                .build();

        EmbeddingSearchResult<TextSegment> resultat = embeddingStore.search(searchRequest);

        return resultat.matches().stream()
                .map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    // Genere l'orientation medicale a partir des symptomes et du profil du patient
    public String genererOrientation(String symptomes, String antecedents, String allergies) {
        String contexteMedical = rechercherContexteMedical(symptomes);
        String prompt = construirePrompt(symptomes, antecedents, allergies, contexteMedical);
        return chatModel.generate(prompt);
    }

    private String construirePrompt(String symptomes, String antecedents, String allergies, String contexte) {
        return """
                Tu es un assistant medical d'orientation pour la plateforme SenMedical au Senegal.
                Ton role est UNIQUEMENT d'orienter le patient vers le bon specialiste et d'evaluer
                le niveau d'urgence. Tu ne dois JAMAIS poser de diagnostic medical precis.

                Documents medicaux de reference :
                %s

                Profil du patient :
                - Antecedents : %s
                - Allergies : %s

                Symptomes decrits par le patient :
                "%s"

                Reponds STRICTEMENT dans ce format, sans rien ajouter d'autre :

                SPECIALITE: [nom du specialiste recommande, ex: Generaliste, Pediatre, Gynecologue, Cardiologue, Ophtalmologue, Urgences]
                GRAVITE: [FAIBLE ou MOYEN ou ELEVE]
                RECOMMANDATION: [2 a 4 phrases expliquant l'orientation et donnant des conseils generaux, sans diagnostic precis. Rappelle que ceci ne remplace pas une consultation medicale.]
                """.formatted(
                contexte.isBlank() ? "Aucun document specifique trouve." : contexte,
                antecedents == null || antecedents.isBlank() ? "Aucun" : antecedents,
                allergies == null || allergies.isBlank() ? "Aucune" : allergies,
                symptomes
        );
    }

    // Parse la reponse texte structuree de Gemini en un objet OrientationResult
    // Version securisee : si Gemini ne respecte pas exactement le format attendu
    // (gravite mal ecrite, valeur inattendue...), on retombe sur des valeurs par defaut
    // plutot que de faire planter la requete
    public OrientationResult parserReponse(String reponseGemini) {
        String specialite = "Generaliste";
        String gravite = "FAIBLE";
        String recommandation = reponseGemini;

        try {
            String[] lignes = reponseGemini.split("\n");
            StringBuilder recommandationBuilder = new StringBuilder();
            boolean dansRecommandation = false;

            for (String ligne : lignes) {
                if (ligne.startsWith("SPECIALITE:")) {
                    specialite = ligne.replace("SPECIALITE:", "").trim();
                } else if (ligne.startsWith("GRAVITE:")) {
                    gravite = ligne.replace("GRAVITE:", "").trim().toUpperCase();
                } else if (ligne.startsWith("RECOMMANDATION:")) {
                    recommandationBuilder.append(ligne.replace("RECOMMANDATION:", "").trim());
                    dansRecommandation = true;
                } else if (dansRecommandation && !ligne.isBlank()) {
                    recommandationBuilder.append(" ").append(ligne.trim());
                }
            }

            if (!recommandationBuilder.isEmpty()) {
                recommandation = recommandationBuilder.toString();
            }
        } catch (Exception e) {
            // valeurs par defaut conservees
        }

        // Securite supplementaire : si la valeur extraite ne correspond a aucun des 3 niveaux
        // valides (FAIBLE/MOYEN/ELEVE), on force FAIBLE plutot que de laisser planter le controleur
        // plus loin avec NiveauGravite.valueOf(...)
        if (!gravite.equals("FAIBLE") && !gravite.equals("MOYEN") && !gravite.equals("ELEVE")) {
            gravite = "FAIBLE";
        }

        if (specialite.isBlank()) {
            specialite = "Generaliste";
        }

        return new OrientationResult(specialite, gravite, recommandation);
    }
}