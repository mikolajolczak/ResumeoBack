package com.example.resumeoback.candidate;

import com.example.resumeoback.position.PositionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidateController {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final List<Candidate> candidates = new ArrayList<>();

    public CandidateController() {
        PositionController positionController = new PositionController();
        candidates.add(new Candidate(UUID.randomUUID().toString(), "John Doe", 85, "2023-10-01", positionController.getRandomPosition()));
        candidates.add(new Candidate(UUID.randomUUID().toString(), "Jane Smith", 90, "2023-10-02", positionController.getRandomPosition()));
        candidates.add(new Candidate(UUID.randomUUID().toString(), "Alice Johnson", 78, "2023-10-03", positionController.getRandomPosition()));
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
            String apiKey = System.getenv("GEMINI_API_KEY"); // <-- ustaw zmienną środowiskową
            if (apiKey == null || apiKey.isBlank()) {
                return "{\"error\":\"Missing API key\"}";
            }

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
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            return "{\"error\":\"Gemini failed: " + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndExtractCandidate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("positionName") String positionName) {

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Only PDF files are allowed"));
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            String prompt = """
               Przeanalizuj przesłany tekst czyjegoś CV i znajdź w nim informację o Imieniu oraz naziwsku osoby, posłuży to jako pole name w wynikowym JSON, następnie oceń w skali 0-100 czy podany kandydat nadaje się na stanowisko %s, posłuży ta ocena później jako pole score w wynikowym JSON. Opowiedz w podanym formacie JSON. Nie podać nic poza JSON:
                {
                  "name": "...",
                  "score": ...,
                  "date": "...",
                  "appointment": "..."
                }

                Tekst CV:
                %s
            """.formatted(positionName, extractedText);

            String rawResponse = sendToGemini(prompt);

            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> fullResponse = mapper.readValue(rawResponse, Map.class);

            List<?> candidatesList = (List<?>) fullResponse.get("candidates");
            if (candidatesList == null || candidatesList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "No candidate response from Gemini"));
            }

            Map<?, ?> contentMap = (Map<?, ?>) ((Map<?, ?>) candidatesList.getFirst()).get("content");
            List<?> partsList = (List<?>) contentMap.get("parts");
            String textContent = (String) ((Map<?, ?>) partsList.getFirst()).get("text");

            String cleanJson = textContent
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            Map<String, Object> data = mapper.readValue(cleanJson, new TypeReference<>() {
            });

            String name = (String) data.getOrDefault("name", "Nieznane imię");
            String date = LocalDate.now().toString();
            String appointment = (String) data.getOrDefault("appointment", "Nieznane stanowisko");

            Object scoreObj = data.get("score");
            int score = 0;
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).intValue();
            } else if (scoreObj instanceof String) {
                try {
                    score = Integer.parseInt((String) scoreObj);
                } catch (NumberFormatException ignored) {
                }
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
