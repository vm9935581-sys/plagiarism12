
package com.plagarism.plagiarism_detection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@SpringBootApplication
@Controller
public class PlagiarismDetectionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlagiarismDetectionSystemApplication.class, args);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Plagiarism Detection System</title>
              <style>
                body {
                    font-family: Arial, sans-serif;
                    background: #eef2f7;
                    padding: 35px;
                    margin: 0;
                }
                main {
                    max-width: 650px;
                    margin: 40px auto;
                    background: white;
                    padding: 28px;
                    border-radius: 12px;
                    box-shadow: 0 5px 20px #0001;
                }
                h1 { color: #234; text-align: center; }
                input, button {
                    box-sizing: border-box;
                    width: 100%;
                    padding: 12px;
                    margin: 12px 0;
                }
                button {
                    background: #2457d6;
                    color: white;
                    border: 0;
                    border-radius: 6px;
                    cursor: pointer;
                }
                button:disabled { opacity: 0.6; }
                #result {
                    margin-top: 20px;
                    padding: 15px;
                    background: #f1f5f9;
                    border-radius: 8px;
                    overflow-wrap: anywhere;
                }
              </style>
            </head>
            <body>
              <main>
                <h1>Plagiarism Detection System</h1>
                <p>Upload two TXT documents to compare their content.</p>

                <form id="uploadForm">
                  <label for="file1">First document</label>
                  <input id="file1" type="file" name="file1"
                         accept=".txt,text/plain" required>

                  <label for="file2">Second document</label>
                  <input id="file2" type="file" name="file2"
                         accept=".txt,text/plain" required>

                  <button type="submit" id="checkButton">
                    Check Plagiarism
                  </button>
                </form>

                <div id="result" hidden></div>
              </main>

              <script>
                const form = document.getElementById('uploadForm');
                const result = document.getElementById('result');
                const button = document.getElementById('checkButton');

                form.addEventListener('submit', async function(event) {
                  event.preventDefault();

                  result.hidden = false;
                  result.textContent = 'Checking documents...';
                  button.disabled = true;

                  try {
                    const response = await fetch('/api/check', {
                      method: 'POST',
                      body: new FormData(form)
                    });

                    const data = await response.json();

                    if (!response.ok) {
                      throw new Error(data.error || 'Request failed');
                    }

                    result.replaceChildren();

                    const heading = document.createElement('h2');
                    heading.textContent =
                        'Similarity: ' + data.similarity + '%';

                    const status = document.createElement('p');
                    status.textContent = 'Result: ' + data.status;

                    const files = document.createElement('p');
                    files.textContent =
                        data.file1 + ' vs ' + data.file2;

                    const note = document.createElement('p');
                    note.textContent =
                        'This compares only the uploaded documents. ' +
                        'It does not search the internet.';

                    result.append(heading, status, files, note);

                  } catch (error) {
                    result.textContent =
                        'Error: ' + error.message;
                  } finally {
                    button.disabled = false;
                  }
                });
              </script>
            </body>
            </html>
            """;
    }

    @PostMapping(
        value = "/api/check",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> check(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2) {

        try {
            validate(file1);
            validate(file2);

            String text1 = new String(
                file1.getBytes(), StandardCharsets.UTF_8);
            String text2 = new String(
                file2.getBytes(), StandardCharsets.UTF_8);

            if (text1.isBlank() || text2.isBlank()) {
                return error(400, "Both documents must contain text.");
            }

            double similarity = similarity(text1, text2);

            String status = similarity >= 70
                ? "High Similarity"
                : similarity >= 35
                    ? "Moderate Similarity"
                    : "Low Similarity";

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("file1", safeName(file1.getOriginalFilename()));
            result.put("file2", safeName(file2.getOriginalFilename()));
            result.put("similarity", similarity);
            result.put("status", status);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return error(400, e.getMessage());
        } catch (IOException e) {
            return error(500, "Could not read uploaded documents.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                "Please upload both documents.");
        }

        if (!safeName(file.getOriginalFilename())
                .toLowerCase(Locale.ROOT).endsWith(".txt")) {
            throw new IllegalArgumentException(
                "Only .txt files are supported.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Each file must be smaller than 5 MB.");
        }
    }

    private String safeName(String name) {
        return name == null ? "document.txt" : name;
    }

    private double similarity(String first, String second) {
        Set<String> words1 = tokens(first);
        Set<String> words2 = tokens(second);

        Set<String> allWords = new HashSet<>(words1);
        allWords.addAll(words2);

        double wordScore = 0.0;

        if (!allWords.isEmpty()) {
            Set<String> commonWords = new HashSet<>(words1);
            commonWords.retainAll(words2);
            wordScore = (double) commonWords.size() / allWords.size();
        }

        Set<String> phrases1 = phrases(first);
        Set<String> phrases2 = phrases(second);

        Set<String> allPhrases = new HashSet<>(phrases1);
        allPhrases.addAll(phrases2);

        double phraseScore = 0.0;

        if (!allPhrases.isEmpty()) {
            Set<String> commonPhrases = new HashSet<>(phrases1);
            commonPhrases.retainAll(phrases2);
            phraseScore =
                (double) commonPhrases.size() / allPhrases.size();
        }

        double score = allPhrases.isEmpty()
            ? wordScore * 100
            : (wordScore * 0.35 + phraseScore * 0.65) * 100;

        return Math.round(score * 100.0) / 100.0;
    }

    private Set<String> tokens(String text) {
        Set<String> words = new HashSet<>();

        for (String word : Pattern.compile("[^\\p{L}\\p{N}]+")
                .split(text.toLowerCase(Locale.ROOT))) {
            if (word.length() > 1) {
                words.add(word);
            }
        }

        return words;
    }

    private Set<String> phrases(String text) {
        String[] words = Pattern.compile("[^\\p{L}\\p{N}]+")
            .split(text.toLowerCase(Locale.ROOT));

        Set<String> result = new HashSet<>();

        for (int i = 0; i + 2 < words.length; i++) {
            if (!words[i].isBlank()
                    && !words[i + 1].isBlank()
                    && !words[i + 2].isBlank()) {
                result.add(
                    words[i] + " " + words[i + 1] + " " + words[i + 2]);
            }
        }

        return result;
    }

    private ResponseEntity<Map<String, Object>> error(
            int status, String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", message);

        return ResponseEntity.status(status).body(body);
    }
}
