package com.ai.resume.service;

import com.ai.resume.dto.AtsScoreDTO;
import com.ai.resume.dto.ResumeAnalysis;
import com.ai.resume.dto.SkillsResponseDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ResumeOrchestratorService {
    private final ResumeAIService resumeAIService;

    public ResumeOrchestratorService(ResumeAIService resumeAIService) {
        this.resumeAIService = resumeAIService;
    }

    public ResumeAnalysis runFullAnalysis(String resumeText, String role) throws Exception {

        CompletableFuture<SkillsResponseDTO> skillsFuture = resumeAIService.extractSkillsAsync(resumeText);
        CompletableFuture<AtsScoreDTO> atsFuture = resumeAIService.calculateAtsScoreAsync(resumeText, role);

        CompletableFuture.allOf(skillsFuture, atsFuture).join();

        return new ResumeAnalysis(skillsFuture.get(), atsFuture.get());
    }
}
