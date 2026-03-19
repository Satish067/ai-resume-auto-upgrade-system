package com.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeSectionsDTO {
    private String professionalSummary;
    @JsonAlias({"skills", "skillCategories"})
    private List<SkillDomainDTO> skillCategories;
    private List<ExperienceDTO> experience;
    private List<ProjectDTO> projects;
    private List<EducationDTO> education;
}
