package com.ai.resume.dto;

public class ResumeAnalysis {

    private SkillsResponseDTO skills;
    private AtsScoreDTO atsScore;

    public ResumeAnalysis(
            SkillsResponseDTO skills,
            AtsScoreDTO atsScore) {

        this.skills = skills;
        this.atsScore = atsScore;
    }

    public SkillsResponseDTO getSkills() {
        return skills;
    }

    public AtsScoreDTO getAtsScore() {
        return atsScore;
    }
}

