package com.plagarism.plagiarism_detection.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plagiarism_reports")
public class PlagiarismReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentName;

    private double similarityPercentage;

    private String plagiarismLevel;

    private String matchedDocumentName;

    @Column(columnDefinition = "TEXT")
    private String matchingText;

    @Column(columnDefinition = "TEXT")
    private String originalText;

    private LocalDateTime checkedAt;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public PlagiarismReport() {
    }


    // ==========================================
    // GET ID
    // ==========================================

    public Long getId() {
        return id;
    }


    // ==========================================
    // DOCUMENT NAME
    // ==========================================

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }


    // ==========================================
    // SIMILARITY
    // ==========================================

    public double getSimilarityPercentage() {
        return similarityPercentage;
    }

    public void setSimilarityPercentage(double similarityPercentage) {
        this.similarityPercentage = similarityPercentage;
    }


    // ==========================================
    // PLAGIARISM LEVEL
    // ==========================================

    public String getPlagiarismLevel() {
        return plagiarismLevel;
    }

    public void setPlagiarismLevel(String plagiarismLevel) {
        this.plagiarismLevel = plagiarismLevel;
    }


    // ==========================================
    // MATCHED DOCUMENT NAME
    // ==========================================

    public String getMatchedDocumentName() {
        return matchedDocumentName;
    }

    public void setMatchedDocumentName(String matchedDocumentName) {
        this.matchedDocumentName = matchedDocumentName;
    }


    // ==========================================
    // MATCHING TEXT
    // ==========================================

    public String getMatchingText() {
        return matchingText;
    }

    public void setMatchingText(String matchingText) {
        this.matchingText = matchingText;
    }


    // ==========================================
    // ORIGINAL TEXT
    // ==========================================

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }


    // ==========================================
    // CHECKED TIME
    // ==========================================

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
}

