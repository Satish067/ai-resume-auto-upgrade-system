package com.ai.resume.dto;

public class ResumeUpgradeResponseDTO {
    private String pdfBase64;
    private AtsScoreDTO atsScore;

    public ResumeUpgradeResponseDTO(String pdfBase64, AtsScoreDTO atsScore) {
        this.pdfBase64 = pdfBase64;
        this.atsScore = atsScore;
    }

    public String getPdfBase64() { return pdfBase64; }
    public AtsScoreDTO getAtsScore() { return atsScore; }
}
