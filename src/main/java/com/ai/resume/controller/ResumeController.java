package com.ai.resume.controller;

import com.ai.resume.constants.IndustryRoles;
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

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeAIService aiService;
    private final ResumeParserService parserService;
    private final ResumePdfService pdfService;
    private final ResumeOrchestratorService orchestrator;


    @PostMapping(value = "/sections", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeSectionsDTO> extractAllSections(@RequestParam("file") MultipartFile file) throws Exception {
        String extractedText = parserService.extractText(file);
        ResumeSectionsDTO sections = aiService.extractSections(extractedText);
        return ResponseEntity.ok(sections);
    }
    @PostMapping(value = "/ats-score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getAtsScore(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "Software Developer") String role) throws Exception {
        String resumeText = parserService.extractText(file);
        AtsScoreDTO score = aiService.calculateAtsScore(resumeText, role);
        return ResponseEntity.ok(score);
    }

    @PostMapping(value = "/upgrade/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateUpgradedResumePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "Software Developer") String role) throws Exception {
        String resumeText = parserService.extractText(file);
        AtsScoreDTO atsScore = aiService.calculateAtsScore(resumeText, role);
        UpgradedResumeDTO upgradedResume = aiService.upgradeResume(resumeText, atsScore.getMissingKeywords(), role);
        byte[] pdfBytes = pdfService.generateResumePdf(upgradedResume);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=upgraded_resume_" + System.currentTimeMillis() + ".pdf")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping(value = "/upgrade", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeUpgradeResponseDTO> upgradeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "Software Developer") String role) throws Exception {
        String resumeText = parserService.extractText(file);
        AtsScoreDTO atsScore = aiService.calculateAtsScore(resumeText, role);
        UpgradedResumeDTO upgradedResume = aiService.upgradeResume(resumeText, atsScore.getMissingKeywords(), role);
        byte[] pdfBytes = pdfService.generateResumePdf(upgradedResume);
        String pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(new ResumeUpgradeResponseDTO(pdfBase64, atsScore));
    }

    @PostMapping(value = "/upgrade/with-keywords", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeUpgradeResponseDTO> upgradeResumeWithKeywords(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "Software Developer") String role,
            @RequestParam String keywords) throws Exception {
        List<String> keywordList = java.util.Arrays.stream(keywords.split(","))
                .map(String::trim).filter(k -> !k.isBlank()).toList();
        String resumeText = parserService.extractText(file);
        AtsScoreDTO atsScore = aiService.calculateAtsScore(resumeText, role);
        UpgradedResumeDTO upgradedResume = aiService.upgradeResume(resumeText, keywordList, role);
        byte[] pdfBytes = pdfService.generateResumePdf(upgradedResume);
        String pdfBase64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(new ResumeUpgradeResponseDTO(pdfBase64, atsScore));
    }
    @GetMapping("/ping")
    public String ping() {
        return "RUNNING";
    }

    @GetMapping("/industries")
    public Map<String, Object> getIndustriesAndRoles() {
        return Map.of(
            "industries", IndustryRoles.getAllIndustries(),
            "roles", IndustryRoles.getAllRoles(),
            "industryRoles", IndustryRoles.INDUSTRY_ROLES
        );
    }

    @PostMapping("/skills")
    public SkillsResponseDTO extractSkills(
            @Valid @RequestBody ResumeRequestDTO request,
            @RequestParam(defaultValue = "Software Engineer") String role)
            throws Exception {

        return aiService.extractSkills(request.getResumeText(), role);
    }

    @PostMapping(value = "/analysis", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResumeAnalysis analyzeResume(
            @Valid @RequestBody String resumeText,
            @RequestParam(defaultValue = "Software Engineer") String role) throws Exception{

        return orchestrator.runFullAnalysis(resumeText, role);
    }

}
