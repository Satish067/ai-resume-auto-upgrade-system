package com.ai.resume.dto;

import java.util.List;

public class SkillsResponseDTO {

    private List<SkillDomainDTO> skillCategories;

    public List<SkillDomainDTO> getSkillCategories() {
        return skillCategories;
    }

    public void setSkillCategories(List<SkillDomainDTO> skillCategories) {
        this.skillCategories = skillCategories;
    }
}
