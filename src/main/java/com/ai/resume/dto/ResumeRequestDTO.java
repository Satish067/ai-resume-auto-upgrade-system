package com.ai.resume.dto;

import jakarta.validation.constraints.NotBlank;

public class ResumeRequestDTO {

    @NotBlank(message = "Resume text cannot be empty")
    private String resumeText;

    @NotBlank
    private String role;

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

