package com.ai.resume.constants;

import java.util.List;
import java.util.Map;

public class IndustryRoles {

    public static final Map<String, List<String>> INDUSTRY_ROLES = Map.of(
            "Information Technology", List.of(
                    "Software Engineer", "Full Stack Developer", "Frontend Developer", 
                    "Backend Developer", "DevOps Engineer", "Data Engineer", 
                    "Machine Learning Engineer", "Cloud Architect", "Cybersecurity Analyst",
                    "Product Manager", "QA Engineer", "Mobile App Developer"
            ),
            
            "Healthcare & Medical", List.of(
                    "Doctor (MBBS)", "Nurse", "Pharmacist", "Medical Technologist",
                    "Physiotherapist", "Radiologist", "Surgeon", "Dentist",
                    "Medical Research Scientist", "Healthcare Administrator", 
                    "Clinical Data Manager", "Biomedical Engineer"
            ),
            
            "Engineering", List.of(
                    "Civil Engineer", "Mechanical Engineer", "Electrical Engineer",
                    "Electronics Engineer", "Chemical Engineer", "Aerospace Engineer",
                    "Environmental Engineer", "Structural Engineer", "Project Engineer",
                    "Quality Engineer", "Design Engineer", "Process Engineer"
            ),
            
            "Finance & Banking", List.of(
                    "Financial Analyst", "Investment Banker", "Accountant", "Auditor",
                    "Risk Manager", "Portfolio Manager", "Credit Analyst", 
                    "Financial Planner", "Tax Consultant", "Compliance Officer",
                    "Actuary", "Treasury Analyst"
            ),
            
            "Marketing & Sales", List.of(
                    "Digital Marketing Manager", "Sales Executive", "Brand Manager",
                    "Content Marketing Specialist", "SEO Specialist", "Social Media Manager",
                    "Business Development Manager", "Account Manager", "Marketing Analyst",
                    "Product Marketing Manager", "Sales Manager", "Customer Success Manager"
            ),
            
            "Education & Academia", List.of(
                    "Teacher", "Professor", "Research Scientist", "Academic Coordinator",
                    "Curriculum Developer", "Educational Consultant", "School Principal",
                    "Training Specialist", "Instructional Designer", "Academic Writer",
                    "Student Counselor", "Education Administrator"
            ),
            
            "Manufacturing & Operations", List.of(
                    "Production Manager", "Operations Manager", "Supply Chain Manager",
                    "Quality Control Manager", "Plant Manager", "Maintenance Engineer",
                    "Logistics Coordinator", "Procurement Specialist", "Safety Manager",
                    "Lean Manufacturing Specialist", "Production Planner", "Warehouse Manager"
            ),
            
            "Legal & Consulting", List.of(
                    "Lawyer", "Legal Advisor", "Corporate Counsel", "Paralegal",
                    "Management Consultant", "Business Analyst", "Strategy Consultant",
                    "Legal Research Associate", "Compliance Manager", "Contract Manager",
                    "Litigation Attorney", "Tax Lawyer"
            ),
            
            "Human Resources", List.of(
                    "HR Manager", "Talent Acquisition Specialist", "HR Business Partner",
                    "Compensation Analyst", "Training & Development Manager", 
                    "Employee Relations Specialist", "HR Generalist", "Recruiter",
                    "Organizational Development Specialist", "HR Analytics Specialist",
                    "Payroll Specialist", "Benefits Administrator"
            ),
            
            "Media & Creative", List.of(
                    "Graphic Designer", "Content Writer", "Video Editor", "Photographer",
                    "UI/UX Designer", "Creative Director", "Copywriter", "Journalist",
                    "Social Media Content Creator", "Art Director", "Web Designer",
                    "Motion Graphics Designer"
            )
    );

    public static final List<String> getAllRoles() {
        return INDUSTRY_ROLES.values().stream()
                .flatMap(List::stream)
                .sorted()
                .toList();
    }

    public static final List<String> getAllIndustries() {
        return INDUSTRY_ROLES.keySet().stream()
                .sorted()
                .toList();
    }

    public static String getIndustryForRole(String role) {
        return INDUSTRY_ROLES.entrySet().stream()
                .filter(entry -> entry.getValue().contains(role))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("General");
    }
}