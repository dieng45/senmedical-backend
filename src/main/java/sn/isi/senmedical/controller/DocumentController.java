// DocumentController.java
// Gestion de la base de connaissances medicale (documents utilises par le RAG)
// Reserve a l'administrateur (voir SecurityConfig : /api/admin/** exige le role ADMIN)
package sn.isi.senmedical.controller;

import sn.isi.senmedical.dto.DocumentResponse;
import sn.isi.senmedical.entity.DocumentMedical;
import sn.isi.senmedical.repository.DocumentMedicalRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/documents")
public class DocumentController {

    private final DocumentMedicalRepository documentMedicalRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public DocumentController(DocumentMedicalRepository documentMedicalRepository,
                              EmbeddingModel embeddingModel,
                              EmbeddingStore<TextSegment> embeddingStore) {
        this.documentMedicalRepository = documentMedicalRepository;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    // GET /api/admin/documents : liste tous les documents medicaux
    @GetMapping
    public ResponseEntity<?> lister() {
        List<DocumentResponse> reponse = documentMedicalRepository.findAll().stream()
                .map(d -> new DocumentResponse(
                        d.getId(), d.getTitre(), d.getSource(), d.getCategorie(), d.getDateAjout().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reponse);
    }

    // POST /api/admin/documents : ajoute un document medical a partir d'un fichier PDF uploade
    // Le texte est extrait automatiquement du PDF, puis vectorise pour le RAG
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> ajouter(@RequestParam("titre") String titre,
                                     @RequestParam(value = "source", required = false) String source,
                                     @RequestParam(value = "categorie", required = false) String categorie,
                                     @RequestParam("fichier") MultipartFile fichier) {

        if (fichier.isEmpty()) {
            return ResponseEntity.badRequest().body("Le fichier PDF est obligatoire");
        }

        // 1. Extraction du texte contenu dans le PDF
        String contenuExtrait;
        try (PDDocument pdf = Loader.loadPDF(fichier.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            contenuExtrait = stripper.getText(pdf);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Impossible de lire le fichier PDF : " + e.getMessage());
        }

        if (contenuExtrait == null || contenuExtrait.isBlank()) {
            return ResponseEntity.badRequest().body("Aucun texte n'a pu etre extrait de ce PDF (peut-etre un scan sans OCR)");
        }

        // 2. Sauvegarde du document (avec le texte extrait) dans la table document_medical
        DocumentMedical document = new DocumentMedical();
        document.setTitre(titre);
        document.setContenu(contenuExtrait);
        document.setSource(source);
        document.setCategorie(categorie);
        documentMedicalRepository.save(document);

        // 3. Vectorisation du contenu extrait et stockage dans pgvector pour la recherche RAG
        TextSegment segment = TextSegment.from(
                contenuExtrait,
                Metadata.from("documentId", document.getId().toString())
        );
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);

        return ResponseEntity.status(201).body(new DocumentResponse(
                document.getId(), document.getTitre(), document.getSource(),
                document.getCategorie(), document.getDateAjout().toString()
        ));
    }

    // DELETE /api/admin/documents/{id} : supprime un document medical
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(@PathVariable Long id) {
        if (!documentMedicalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        documentMedicalRepository.deleteById(id);
        return ResponseEntity.ok("Document supprime avec succes");
    }
}