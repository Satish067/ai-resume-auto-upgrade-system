package com.ai.resume.controller;

import com.ai.resume.dto.*;
import com.ai.resume.service.ResumeAIService;
import com.ai.resume.service.ResumeOrchestratorService;
import com.ai.resume.service.ResumeParserService;
import com.ai.resume.service.ResumePdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeAIService aiService;
    private final ResumeParserService parserService;
    private final ResumePdfService pdfService;
    private final ResumeOrchestratorService orchestrator;


    public ResponseEntity<?> uploadResume(@RequestParam("file")MultipartFile file) throws Exception{
    String extractedText = parserService.extractText(file);
    ResumeSectionsDTO sections = aiService.extractSections(extractedText);

    return ResponseEntity.ok(sections);
    }
    @PostMapping("/ats-score")
    public ResponseEntity<?> getAtsScore(@RequestParam("file")MultipartFile file) throws Exception {
    String resumeText = parserService.extractText(file);
    AtsScoreDTO score = aiService.calculateAtsScore(resumeText, "Java Developer");

    return ResponseEntity.ok(score);
    }

    @PostMapping("/upgrade/pdf")
    public ResponseEntity<byte[]> generateUpgradedResumePdf(@RequestParam("file")MultipartFile file) throws Exception {

        String resumeText = parserService.extractText(file);
        AtsScoreDTO atsScore = aiService.calculateAtsScore(resumeText, "Java Developer");
        UpgradedResumeDTO upgradedResume = aiService.upgradeResume(resumeText, atsScore.getMissingKeywords(), "Java Developer");
        byte[] pdfBytes = pdfService.generateResumePdf(upgradedResume);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=upgraded_resume.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    @GetMapping("/ping")
    public String ping() {
        return "RUNNING";
    }

    @PostMapping("/skills")
    public SkillsResponseDTO extractSkills(
            @Valid @RequestBody ResumeRequestDTO request)
            throws Exception {

        return aiService.extractSkills(request.getResumeText());
    }

    @PostMapping(value = "/analysis", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResumeAnalysis analyzeResume(@Valid @RequestBody String resumeText) throws Exception{

        return orchestrator.runFullAnalysis(
                resumeText,
                "Software Engineer" // temporary for testing
        );
    }

}
