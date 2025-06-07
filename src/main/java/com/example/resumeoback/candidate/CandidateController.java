package com.example.resumeoback.candidate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidateController {

    private final List<Candidate> candidates = new ArrayList<>();

    public CandidateController() {
        candidates.add(new Candidate(UUID.randomUUID().toString(), "John Doe", 85, "2023-10-01", "junior developer"));
        candidates.add(new Candidate(UUID.randomUUID().toString(), "Jane Smith", 90, "2023-10-02", "senior developer"));
        candidates.add(new Candidate(UUID.randomUUID().toString(), "Alice Johnson", 78, "2023-10-03", "project manager"));
    }

    @GetMapping
    public List<Candidate> getCandidates() {
        return candidates;
    }

    @PostMapping
    public Candidate addCandidate(@RequestBody Candidate candidate) {
        candidate.setId(UUID.randomUUID().toString());
        candidates.add(candidate);
        return candidate;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable String id) {
        boolean removed = candidates.removeIf(c -> c.getId().equals(id));
        return removed ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private String sendToGemini(String prompt) {
        try {
            String apiKey = "";
            String requestBody = """
            {
              "contents": [{
                "parts": [{
                  "text": "%s"
                }]
              }]
            }
            """.formatted(prompt.replace("\"", "'"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            return "{\"error\":\"Gemini failed: " + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndExtractCandidate(@RequestParam("file") MultipartFile file) {
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Only PDF files are allowed"));
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            String prompt = """
                Przeanalizuj poniższy tekst CV i odpowiedz tylko w czystym JSON w następującym formacie:
                {
                  "name": "...",
                  "score": ...,
                  "date": "...",
                  "appointment": "..."
                }

                Tekst CV:
                %s
            """.formatted(extractedText);

            String response = sendToGemini(prompt);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(response, Map.class);

            String name = (String) data.getOrDefault("name", "Nieznane imię");
            String date = (String) data.getOrDefault("date", LocalDate.now().toString());
            String appointment = (String) data.getOrDefault("appointment", "Nieznane stanowisko");

            Object scoreObj = data.get("score");
            int score = 0;
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).intValue();
            } else if (scoreObj instanceof String) {
                try {
                    score = Integer.parseInt((String) scoreObj);
                } catch (NumberFormatException ignored) {}
            }

            Candidate candidate = new Candidate(
                    UUID.randomUUID().toString(),
                    name,
                    score,
                    date,
                    appointment
            );

            candidates.add(candidate);

            return ResponseEntity.ok(candidate);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "PDF/Gemini failed: " + e.getMessage()));
        }
    }

}
