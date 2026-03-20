package com.ai.resume.constants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class IndustryRolesTest {

    @Test
    public void testGetAllRoles() {
        List<String> roles = IndustryRoles.getAllRoles();
        assertFalse(roles.isEmpty());
        assertTrue(roles.contains("Software Engineer"));
        assertTrue(roles.contains("Civil Engineer"));
        assertTrue(roles.contains("Doctor (MBBS)"));
        assertTrue(roles.contains("Financial Analyst"));
    }

    @Test
    public void testGetAllIndustries() {
        List<String> industries = IndustryRoles.getAllIndustries();
        assertFalse(industries.isEmpty());
        assertTrue(industries.contains("Information Technology"));
        assertTrue(industries.contains("Healthcare & Medical"));
        assertTrue(industries.contains("Engineering"));
        assertTrue(industries.contains("Finance & Banking"));
    }

    @Test
    public void testGetIndustryForRole() {
        assertEquals("Information Technology", IndustryRoles.getIndustryForRole("Software Engineer"));
        assertEquals("Healthcare & Medical", IndustryRoles.getIndustryForRole("Doctor (MBBS)"));
        assertEquals("Engineering", IndustryRoles.getIndustryForRole("Civil Engineer"));
        assertEquals("Finance & Banking", IndustryRoles.getIndustryForRole("Financial Analyst"));
        assertEquals("General", IndustryRoles.getIndustryForRole("Unknown Role"));
    }

    @Test
    public void testSkillDomainsForIndustry() {
        List<String> itSkills = SkillDomains.getSkillDomainsForIndustry("Information Technology");
        assertTrue(itSkills.contains("Programming Languages"));
        assertTrue(itSkills.contains("Cloud & DevOps"));

        List<String> healthcareSkills = SkillDomains.getSkillDomainsForIndustry("Healthcare & Medical");
        assertTrue(healthcareSkills.contains("Clinical Skills"));
        assertTrue(healthcareSkills.contains("Patient Care"));

        List<String> engineeringSkills = SkillDomains.getSkillDomainsForIndustry("Engineering");
        assertTrue(engineeringSkills.contains("Technical Skills"));
        assertTrue(engineeringSkills.contains("Design & Modeling"));
    }

    @Test
    public void testSkillDomainsForRole() {
        List<String> softwareEngineerSkills = SkillDomains.getSkillDomainsForRole("Software Engineer");
        assertTrue(softwareEngineerSkills.contains("Programming Languages"));

        List<String> doctorSkills = SkillDomains.getSkillDomainsForRole("Doctor (MBBS)");
        assertTrue(doctorSkills.contains("Clinical Skills"));

        List<String> civilEngineerSkills = SkillDomains.getSkillDomainsForRole("Civil Engineer");
        assertTrue(civilEngineerSkills.contains("Technical Skills"));
    }
}