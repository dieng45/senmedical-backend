// GeminiConfig.java
// Configure le modele de langage Gemini et le store vectoriel pgvector comme beans Spring
// Utilises ensuite par le service RAG pour analyser les symptomes du patient
package sn.isi.senmedical.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Le modele de chat utilise pour generer l'orientation medicale
    @Bean
    public ChatLanguageModel chatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-3.6-flash")
                .temperature(0.3)
                .build();
    }

    // Le modele qui transforme le texte en vecteur (embedding) pour le RAG
    // gemini-embedding-001 produit des vecteurs de dimension 3072 par defaut
    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-embedding-001")
                .build();
    }

    // Le store vectoriel pgvector : stocke les embeddings des documents medicaux pour le RAG
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        String cleanUrl = datasourceUrl.replace("jdbc:postgresql://", "");
        String hostPort = cleanUrl.split("/")[0];
        String database = cleanUrl.split("/")[1];
        String host = hostPort.split(":")[0];
        int port = Integer.parseInt(hostPort.split(":")[1]);

        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(dbUsername)
                .password(dbPassword)
                .table("document_embeddings")
                .dimension(3072) // dimension par defaut de gemini-embedding-001
                .build();
    }
}