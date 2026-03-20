package com.ai.resume.constants;

import java.util.List;
import java.util.Map;

public class SkillDomains {

    // Legacy IT-focused domains for backward compatibility
    public static final List<String> ALLOWED = List.of(
            "Backend Development",
            "Frontend Development",
            "Databases",
            "Cloud & DevOps",
            "AI / ML",
            "Tools & Platforms",
            "Soft Skills"
    );

    // Industry-specific skill domains
    public static final Map<String, List<String>> INDUSTRY_SKILL_DOMAINS = Map.of(
            "Information Technology", List.of(
                    "Programming Languages", "Backend Development", "Frontend Development",
                    "Databases", "Cloud & DevOps", "AI / ML", "Mobile Development",
                    "Cybersecurity", "Tools & Platforms", "Soft Skills"
            ),
            
            "Healthcare & Medical", List.of(
                    "Clinical Skills", "Medical Procedures", "Diagnostic Tools",
                    "Patient Care", "Medical Software", "Laboratory Techniques",
                    "Pharmaceutical Knowledge", "Medical Research", "Soft Skills"
            ),
            
            "Engineering", List.of(
                    "Technical Skills", "Design & Modeling", "Project Management",
                    "Quality Control", "Safety & Compliance", "Engineering Software",
                    "Materials & Testing", "Problem Solving", "Soft Skills"
            ),
            
            "Finance & Banking", List.of(
                    "Financial Analysis", "Risk Management", "Investment Strategies",
                    "Accounting & Auditing", "Regulatory Compliance", "Financial Software",
                    "Market Research", "Client Relations", "Soft Skills"
            ),
            
            "Marketing & Sales", List.of(
                    "Digital Marketing", "Content Creation", "Sales Techniques",
                    "Market Analysis", "Brand Management", "Marketing Tools",
                    "Customer Relationship", "Campaign Management", "Soft Skills"
            ),
            
            "Education & Academia", List.of(
                    "Teaching Methods", "Curriculum Development", "Research Skills",
                    "Educational Technology", "Assessment & Evaluation", "Subject Expertise",
                    "Student Engagement", "Academic Writing", "Soft Skills"
            ),
            
            "Manufacturing & Operations", List.of(
                    "Production Management", "Quality Control", "Supply Chain",
                    "Lean Manufacturing", "Safety Management", "Industrial Software",
                    "Process Optimization", "Equipment Maintenance", "Soft Skills"
            ),
            
            "Legal & Consulting", List.of(
                    "Legal Research", "Case Management", "Contract Law",
                    "Litigation", "Compliance", "Legal Software",
                    "Client Advisory", "Regulatory Knowledge", "Soft Skills"
            ),
            
            "Human Resources", List.of(
                    "Talent Acquisition", "Employee Relations", "Performance Management",
                    "Compensation & Benefits", "Training & Development", "HR Software",
                    "Labor Law", "Organizational Development", "Soft Skills"
            ),
            
            "Media & Creative", List.of(
                    "Design Software", "Creative Concepts", "Content Creation",
                    "Visual Communication", "Brand Identity", "Digital Media",
                    "Project Management", "Client Collaboration", "Soft Skills"
            )
    );

    // Universal soft skills applicable to all industries
    public static final List<String> UNIVERSAL_SOFT_SKILLS = List.of(
            "Communication", "Leadership", "Problem Solving", "Team Collaboration",
            "Time Management", "Adaptability", "Critical Thinking", "Project Management",
            "Analytical Skills", "Attention to Detail", "Customer Service", "Creativity"
    );

    public static List<String> getSkillDomainsForIndustry(String industry) {
        return INDUSTRY_SKILL_DOMAINS.getOrDefault(industry, ALLOWED);
    }

    public static List<String> getSkillDomainsForRole(String role) {
        String industry = IndustryRoles.getIndustryForRole(role);
        return getSkillDomainsForIndustry(industry);
    }
}
