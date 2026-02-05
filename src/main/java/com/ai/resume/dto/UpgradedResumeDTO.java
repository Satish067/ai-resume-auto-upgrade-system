package com.ai.resume.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpgradedResumeDTO {
    private String professionalSummary;
    private List<SkillDomainDTO> skillCategories;
    private List<ExperienceDTO> experience;
    private List<ProjectDTO> projects;
    private List<EducationDTO> education;

    public String getProfessionalSummary() {

        return professionalSummary;
    }

    public void setProfessionalSummary(String professionalSummary) {
        this.professionalSummary = professionalSummary;
    }

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

    public List<EducationDTO> getEducation() {
        return education;
    }

    public void setEducation(List<EducationDTO> education) {
        this.education = education;
    }
}
