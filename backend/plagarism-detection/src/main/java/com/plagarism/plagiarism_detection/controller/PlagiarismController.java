package com.plagarism.plagiarism_detection.controller;

import com.plagarism.plagiarism_detection.model.PlagiarismReport;
import com.plagarism.plagiarism_detection.service.PlagiarismService;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/plagiarism")
@CrossOrigin(origins = "*")
public class PlagiarismController {

    private final PlagiarismService plagiarismService;

    public PlagiarismController(PlagiarismService plagiarismService) {
        this.plagiarismService = plagiarismService;
    }

    // =====================================================
    // TEXT CHECK
    // =====================================================

    @PostMapping("/check")
    public PlagiarismReport checkPlagiarism(
            @RequestParam String documentName,
            @RequestParam String text) {

        return plagiarismService.checkPlagiarism(
                documentName,
                text
        );
    }

    // =====================================================
    // FILE CHECK - TXT / PDF / DOCX
    // =====================================================

    @PostMapping("/check-file")
    public PlagiarismReport checkFile(
            @RequestParam("file") MultipartFile file) {

        try {

            if (file.isEmpty()) {
                throw new RuntimeException("File is empty.");
            }

            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isBlank()) {
                throw new RuntimeException("File name is missing.");
            }

            String text = extractText(file);

            if (text == null || text.isBlank()) {
                throw new RuntimeException(
                        "No readable text found in the file."
                );
            }

            return plagiarismService.checkPlagiarism(
                    fileName,
                    text
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to process file: " + e.getMessage()
            );
        }
    }

    // =====================================================
    // FILE TEXT EXTRACTION
    // =====================================================

    private String extractText(MultipartFile file)
            throws IOException {

        String fileName = file.getOriginalFilename()
                .toLowerCase();

        byte[] fileBytes = file.getBytes();

        // -------------------------------------------------
        // TXT FILE
        // -------------------------------------------------

        if (fileName.endsWith(".txt")) {

            return new String(
                    fileBytes,
                    StandardCharsets.UTF_8
            );
        }

        // -------------------------------------------------
        // DOCX FILE
        // -------------------------------------------------

        if (fileName.endsWith(".docx")) {

            StringBuilder text = new StringBuilder();

            try (XWPFDocument document =
                         new XWPFDocument(
                                 new ByteArrayInputStream(fileBytes))) {

                for (XWPFParagraph paragraph :
                        document.getParagraphs()) {

                    String paragraphText =
                            paragraph.getText();

                    if (paragraphText != null &&
                            !paragraphText.isBlank()) {

                        text.append(paragraphText)
                                .append("\n");
                    }
                }
            }

            return text.toString();
        }

        // -------------------------------------------------
        // PDF FILE
        // -------------------------------------------------

        if (fileName.endsWith(".pdf")) {

            try (var document =
                         Loader.loadPDF(fileBytes)) {

                PDFTextStripper stripper =
                        new PDFTextStripper();

                return stripper.getText(document);
            }
        }

        // -------------------------------------------------
        // UNSUPPORTED FILE
        // -------------------------------------------------

        throw new RuntimeException(
                "Unsupported file type. " +
                "Please upload TXT, PDF, or DOCX."
        );
    }

    // =====================================================
    // GET ALL REPORTS
    // =====================================================

    @GetMapping("/reports")
    public List<PlagiarismReport> getAllReports() {

        return plagiarismService.getAllReports();
    }

    // =====================================================
    // DELETE REPORT
    // =====================================================

    @DeleteMapping("/reports/{id}")
    public String deleteReport(
            @PathVariable Long id) {

        plagiarismService.deleteReport(id);

        return "Report deleted successfully!";
    }
}

