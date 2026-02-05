package com.ai.resume.dto;

import java.util.List;

public class SkillDomainDTO {
    private String domain;
    private List<SkillItemDTO> skills;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<SkillItemDTO> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillItemDTO> skills) {
        this.skills = skills;
    }
}
