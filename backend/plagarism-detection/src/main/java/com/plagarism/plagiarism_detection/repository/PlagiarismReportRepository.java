package com.plagarism.plagiarism_detection.repository;

import com.plagarism.plagiarism_detection.model.PlagiarismReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlagiarismReportRepository
        extends JpaRepository<PlagiarismReport, Long> {
}

