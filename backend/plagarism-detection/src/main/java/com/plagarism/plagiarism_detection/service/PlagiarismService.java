package com.plagarism.plagiarism_detection.service;

import com.plagarism.plagiarism_detection.model.PlagiarismReport;
import com.plagarism.plagiarism_detection.repository.PlagiarismReportRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlagiarismService {

    private final PlagiarismReportRepository repository;

    public PlagiarismService(
            PlagiarismReportRepository repository) {

        this.repository = repository;
    }

    // ============================================================
    // MAIN PLAGIARISM CHECK
    // ============================================================

    public PlagiarismReport checkPlagiarism(
            String documentName,
            String text) {

        PlagiarismReport report =
                new PlagiarismReport();

        // --------------------------------------------------------
        // Empty text check
        // --------------------------------------------------------

        if (text == null || text.isBlank()) {

            report.setDocumentName(documentName);
            report.setOriginalText(text);
            report.setSimilarityPercentage(0.0);
            report.setPlagiarismLevel("Low");
            report.setMatchedDocumentName(
                    "No matching document"
            );
            report.setMatchingText(
                    "No readable text found."
            );
            report.setCheckedAt(
                    LocalDateTime.now()
            );

            return repository.save(report);
        }

        // --------------------------------------------------------
        // Get current document sentences
        // --------------------------------------------------------

        List<String> currentSentences =
                splitIntoSentences(text);

        // --------------------------------------------------------
        // Get all previously checked documents
        // --------------------------------------------------------

        List<PlagiarismReport> previousReports =
                repository.findAll();

        double highestOverallSimilarity = 0.0;

        String bestMatchedDocument =
                "No matching document";

        List<String> bestMatchingDetails =
                new ArrayList<>();

        // --------------------------------------------------------
        // Compare current document with every previous document
        // --------------------------------------------------------

        for (PlagiarismReport oldReport :
                previousReports) {

            if (oldReport.getOriginalText() == null ||
                    oldReport.getOriginalText().isBlank()) {

                continue;
            }

            List<String> oldSentences =
                    splitIntoSentences(
                            oldReport.getOriginalText()
                    );

            if (oldSentences.isEmpty()) {
                continue;
            }

            // ----------------------------------------------------
            // Compare every current sentence
            // ----------------------------------------------------

            double totalSentenceSimilarity = 0.0;

            int comparedSentences = 0;

            List<String> matchingDetails =
                    new ArrayList<>();

            for (String currentSentence :
                    currentSentences) {

                if (currentSentence.isBlank()) {
                    continue;
                }

                double bestSentenceSimilarity = 0.0;

                String bestSentenceMatch = "";

                // ------------------------------------------------
                // Compare current sentence with all old sentences
                // ------------------------------------------------

                for (String oldSentence :
                        oldSentences) {

                    if (oldSentence.isBlank()) {
                        continue;
                    }

                    double similarity =
                            calculateSentenceSimilarity(
                                    currentSentence,
                                    oldSentence
                            );

                    if (similarity >
                            bestSentenceSimilarity) {

                        bestSentenceSimilarity =
                                similarity;

                        bestSentenceMatch =
                                oldSentence;
                    }
                }

                totalSentenceSimilarity +=
                        bestSentenceSimilarity;

                comparedSentences++;

                // ------------------------------------------------
                // Store meaningful sentence matches
                // ------------------------------------------------

                if (bestSentenceSimilarity >= 30.0) {

                    matchingDetails.add(
                            "Original: "
                                    + currentSentence
                                    + "\n"
                                    + "Matched: "
                                    + bestSentenceMatch
                                    + "\n"
                                    + "Sentence Similarity: "
                                    + String.format(
                                            "%.1f",
                                            bestSentenceSimilarity
                                    )
                                    + "%\n"
                    );
                }
            }

            // ----------------------------------------------------
            // Calculate overall similarity
            // ----------------------------------------------------

            double overallSimilarity = 0.0;

            if (comparedSentences > 0) {

                overallSimilarity =
                        totalSentenceSimilarity /
                                comparedSentences;
            }

            overallSimilarity =
                    Math.round(
                            overallSimilarity * 100.0
                    ) / 100.0;

            // ----------------------------------------------------
            // Keep document with highest similarity
            // ----------------------------------------------------

            if (overallSimilarity >
                    highestOverallSimilarity) {

                highestOverallSimilarity =
                        overallSimilarity;

                bestMatchedDocument =
                        oldReport.getDocumentName();

                bestMatchingDetails =
                        new ArrayList<>(
                                matchingDetails
                        );
            }
        }

        // ============================================================
        // PLAGIARISM LEVEL
        // ============================================================

        String plagiarismLevel;

        if (highestOverallSimilarity >= 70.0) {

            plagiarismLevel = "High";

        } else if (highestOverallSimilarity >= 30.0) {

            plagiarismLevel = "Medium";

        } else {

            plagiarismLevel = "Low";
        }

        // ============================================================
        // MATCHING TEXT
        // ============================================================

        String matchingText;

        if (bestMatchingDetails.isEmpty()) {

            matchingText =
                    "No significant matching sentences found.";

            bestMatchedDocument =
                    "No matching document";

        } else {

            matchingText =
                    String.join(
                            "\n",
                            bestMatchingDetails
                    );
        }

        // ============================================================
        // CREATE REPORT
        // ============================================================

        report.setDocumentName(
                documentName
        );

        report.setOriginalText(
                text
        );

        report.setSimilarityPercentage(
                highestOverallSimilarity
        );

        report.setPlagiarismLevel(
                plagiarismLevel
        );

        report.setMatchedDocumentName(
                bestMatchedDocument
        );

        report.setMatchingText(
                matchingText
        );

        report.setCheckedAt(
                LocalDateTime.now()
        );

        return repository.save(report);
    }

    // ============================================================
    // GET ALL REPORTS
    // ============================================================

    public List<PlagiarismReport> getAllReports() {

        return repository.findAll();
    }

    // ============================================================
    // DELETE REPORT
    // ============================================================

    public void deleteReport(Long id) {

        repository.deleteById(id);
    }

    // ============================================================
    // SPLIT TEXT INTO SENTENCES
    // ============================================================

    private List<String> splitIntoSentences(
            String text) {

        if (text == null ||
                text.isBlank()) {

            return new ArrayList<>();
        }

        return Arrays.stream(
                    text
                            .replaceAll(
                                    "\\s+",
                                    " "
                            )
                            .split(
                                    "(?<=[.!?])\\s+"
                            )
                )
                .map(String::trim)
                .filter(sentence ->
                        !sentence.isBlank()
                )
                .collect(
                        Collectors.toList()
                );
    }

    // ============================================================
    // SENTENCE SIMILARITY
    // ============================================================

    private double calculateSentenceSimilarity(
            String sentence1,
            String sentence2) {

        String clean1 =
                cleanText(sentence1);

        String clean2 =
                cleanText(sentence2);

        if (clean1.isBlank() ||
                clean2.isBlank()) {

            return 0.0;
        }

        // --------------------------------------------------------
        // Get words
        // --------------------------------------------------------

        Set<String> words1 =
                getWords(clean1);

        Set<String> words2 =
                getWords(clean2);

        if (words1.isEmpty() ||
                words2.isEmpty()) {

            return 0.0;
        }

        // --------------------------------------------------------
        // Common words
        // --------------------------------------------------------

        Set<String> commonWords =
                new HashSet<>(words1);

        commonWords.retainAll(words2);

        // --------------------------------------------------------
        // Union of words
        // --------------------------------------------------------

        Set<String> allWords =
                new HashSet<>(words1);

        allWords.addAll(words2);

        if (allWords.isEmpty()) {

            return 0.0;
        }

        // --------------------------------------------------------
        // Jaccard similarity
        // --------------------------------------------------------

        double jaccard =
                (double) commonWords.size()
                        / allWords.size();

        // --------------------------------------------------------
        // Containment similarity
        // --------------------------------------------------------

        double containment =
                (double) commonWords.size()
                        /
                        Math.min(
                                words1.size(),
                                words2.size()
                        );

        // --------------------------------------------------------
        // Character similarity
        // --------------------------------------------------------

        double characterSimilarity =
                calculateCharacterSimilarity(
                        clean1,
                        clean2
                );

        // --------------------------------------------------------
        // Combined score
        // --------------------------------------------------------

        double finalScore =
                (
                        (jaccard * 0.50)
                                +
                        (containment * 0.30)
                                +
                        (characterSimilarity * 0.20)
                )
                        * 100.0;

        // --------------------------------------------------------
        // Prevent very small accidental matches
        // --------------------------------------------------------

        if (commonWords.size() < 2) {

            return 0.0;
        }

        return Math.min(
                100.0,
                finalScore
        );
    }

    // ============================================================
    // CHARACTER SIMILARITY
    // ============================================================

    private double calculateCharacterSimilarity(
            String text1,
            String text2) {

        if (text1.equals(text2)) {

            return 1.0;
        }

        int maxLength =
                Math.max(
                        text1.length(),
                        text2.length()
                );

        if (maxLength == 0) {

            return 1.0;
        }

        int distance =
                levenshteinDistance(
                        text1,
                        text2
                );

        return 1.0 -
                ((double) distance / maxLength);
    }

    // ============================================================
    // LEVENSHTEIN DISTANCE
    // ============================================================

    private int levenshteinDistance(
            String text1,
            String text2) {

        int length1 =
                text1.length();

        int length2 =
                text2.length();

        int[][] matrix =
                new int[
                        length1 + 1
                ][
                        length2 + 1
                ];

        // --------------------------------------------------------
        // First column
        // --------------------------------------------------------

        for (int i = 0;
                i <= length1;
                i++) {

            matrix[i][0] = i;
        }

        // --------------------------------------------------------
        // First row
        // --------------------------------------------------------

        for (int j = 0;
                j <= length2;
                j++) {

            matrix[0][j] = j;
        }

        // --------------------------------------------------------
        // Calculate distance
        // --------------------------------------------------------

        for (int i = 1;
                i <= length1;
                i++) {

            for (int j = 1;
                    j <= length2;
                    j++) {

                int cost =
                        text1.charAt(i - 1)
                                ==
                        text2.charAt(j - 1)
                                ? 0
                                : 1;

                matrix[i][j] =
                        Math.min(
                                Math.min(
                                        matrix[i - 1][j]
                                                + 1,
                                        matrix[i][j - 1]
                                                + 1
                                ),
                                matrix[i - 1][j - 1]
                                        + cost
                        );
            }
        }

        return matrix[
                length1
        ][
                length2
        ];
    }

    // ============================================================
    // CLEAN TEXT
    // ============================================================

    private String cleanText(
            String text) {

        if (text == null) {

            return "";
        }

        return text
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    // ============================================================
    // GET WORDS
    // ============================================================

    private Set<String> getWords(
            String text) {

        return Arrays.stream(
                    text.split("\\s+")
                )
                .filter(word ->
                        word.length() > 2
                )
                .collect(
                        Collectors.toSet()
                );
    }
}

