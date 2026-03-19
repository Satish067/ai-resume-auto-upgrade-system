package com.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpgradedResumeDTO {
    private PersonalDetailsDTO personalDetails;
    private String summaryType;
    private String professionalSummary;
    @JsonAlias({"skills", "skillCategories"})
    private List<SkillDomainDTO> skillCategories;
    private List<ExperienceDTO> experience;
    private List<ProjectDTO> projects;
    private List<EducationDTO> education;
    @JsonAlias({"certifications", "certificates", "courses"})
    private List<CertificationDTO> certifications;
    @JsonAlias({"achievements", "awards", "honors", "accomplishments"})
    private List<AchievementDTO> achievements;

    public PersonalDetailsDTO getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(PersonalDetailsDTO personalDetails) { this.personalDetails = personalDetails; }

    public String getSummaryType() { return summaryType; }
    public void setSummaryType(String summaryType) { this.summaryType = summaryType; }

    public String getProfessionalSummary() { return professionalSummary; }
    public void setProfessionalSummary(String professionalSummary) { this.professionalSummary = professionalSummary; }

    public List<SkillDomainDTO> getSkillCategories() {
        return skillCategories;
    }

    public void setSkillCategories(List<SkillDomainDTO> skillCategories) {
        this.skillCategories = skillCategories;
    }

    public List<ExperienceDTO> getExperience() {
        return experience;
    }

    public void setExperience(List<ExperienceDTO> experience) {
        this.experience = experience;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public List<EducationDTO> getEducation() { return education; }
    public void setEducation(List<EducationDTO> education) { this.education = education; }

    public List<CertificationDTO> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationDTO> certifications) { this.certifications = certifications; }

    public List<AchievementDTO> getAchievements() { return achievements; }
    public void setAchievements(List<AchievementDTO> achievements) { this.achievements = achievements; }
}
