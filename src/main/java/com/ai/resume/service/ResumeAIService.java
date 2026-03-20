package com.ai.resume.service;

import com.ai.resume.ai.ResumeSchema;
import com.ai.resume.config.OpenAiConfig;
import com.ai.resume.constants.IndustryRoles;
import com.ai.resume.constants.SkillDomains;
import com.ai.resume.dto.AtsScoreDTO;
import com.ai.resume.dto.ResumeSectionsDTO;
import com.ai.resume.dto.SkillsResponseDTO;
import com.ai.resume.dto.UpgradedResumeDTO;
import okhttp3.*;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ResumeAIService {

    private final OpenAiConfig openAiConfig;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Logger log =
            LoggerFactory.getLogger(ResumeAIService.class);



    @PostConstruct
    public void checkApiKey() {
        String key = openAiConfig.getApiKey();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "OpenAI API key not loaded. Check application.properties");
        }
        log.info("API key loaded. Length={}, Starts with={}", key.length(), key.substring(0, 10));
    }

    public ResumeAIService(OpenAiConfig openAiConfig) {
        this.openAiConfig = openAiConfig;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

    }

    // For extractiong Sections, Keywords from Resume
    public ResumeSectionsDTO extractSections(String resumeText) throws Exception {

        String prompt = buildSectionExtractionPrompt(resumeText);

        String requestBody = buildRequestBody(prompt);

        String aiJson = getAIJson(requestBody);

        return objectMapper.readValue(aiJson, ResumeSectionsDTO.class);
    }


    private String buildSectionExtractionPrompt(String resumeText){
        return """
        You are an ATS-grade resume parser.

        TASK:
        Extract ALL modules/sections present in the resume and return them in STRICT JSON.

        IMPORTANT:
        - Return ONLY valid JSON (no markdown, no commentary).
        - Use these exact field names and types.

        JSON FORMAT:
        {
          "professionalSummary": "string",
          "skillCategories": [
            {
              "domain": "string",
              "skills": [
                { "name": "string", "explanation": "string" }
              ]
            }
          ],
          "experience": [
            { "company": "string", "role": "string", "duration": "string", "description": "string" }
          ],
          "projects": [
            { "name": "string", "description": "string", "techStack": ["string"] }
          ],
          "education": [
            { "degree": "string", "institution": "string", "year": "string" }
          ]
        }

        RESUME:
        <<<
        %s
        >>>
        """.formatted(resumeText);
    }


    private String buildRequestBody(String prompt) throws Exception{
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", openAiConfig.getModel());

        ArrayNode messages = root.putArray("messages");

        ObjectNode system = messages.addObject();

        system.put("role", "system");
        system.put("content", """
You are a backend JSON API.

Return ONLY valid JSON.
No markdown.
No explanations.
No extra text.

The response MUST be directly parseable by Jackson.

If the JSON is invalid, the application will crash.
""");

        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", prompt);

        return objectMapper.writeValueAsString(root);
    }


    private String extractContentFromResponse(String response) throws Exception {

        JsonNode root = objectMapper.readTree(response);

        if (root.has("error")) {
            throw new RuntimeException(
                    "OpenAI API Error: " + root.get("error").toString()
            );
        }

        JsonNode choices = root.get("choices");

        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException(
                    "Invalid OpenAI response — 'choices' missing.\nResponse = " + response
            );
        }

        JsonNode message = choices.get(0).get("message");

        if (message == null || !message.has("content")) {
            throw new RuntimeException(
                    "Invalid OpenAI response — 'message.content' missing.\nResponse = " + response
            );
        }

        return message.get("content").asText();
    }


    // For ATS Score
    private String buildAtsScoringPrompt(String resumeText, String role) {
        String industry = IndustryRoles.getIndustryForRole(role);
        
        return """
                You are an ATS (Applicant Tracking System) used by recruiters in the %s industry.
                
                Evaluate the resume for the role of %s.
                
                Industry-specific evaluation criteria for %s:
                - Technical skills relevant to %s
                - Industry-specific certifications and qualifications
                - Relevant experience and achievements
                - Domain knowledge and expertise
                - Professional development in %s field
                
                Scoring rules:
                - Score between 0 and 100
                - Consider keyword match, skills, experience and projects relevant to %s
                - Penalize missing core skills for %s role
                - Be strict and realistic based on %s industry standards
                - Focus on industry-relevant accomplishments
                
                Return STRICT JSON only in this format:
                {
                "score": 0,
                "missingKeywords": [],
                "improvementSuggestions": []
                }
                
                Resume:
                %s
                """.formatted(industry, role, industry, role, industry, role, role, industry, resumeText);
    }


    public AtsScoreDTO calculateAtsScore(String resumeText, String role)
            throws Exception {

        String prompt = buildAtsScoringPrompt(resumeText, role);
        String requestBody = buildRequestBody(prompt);

        // ✅ single OpenAI call
        String aiJson = getAIJson(requestBody);

        // 🔒 ATS returns RAW JSON → parse directly
        return objectMapper.readValue(aiJson, AtsScoreDTO.class);
    }


    // For Resume Auto Upgradation
    private String buildResumeUpgradePrompt(String resumeText, List<String> missingKeywords, String role) {
        String industry = IndustryRoles.getIndustryForRole(role);
        List<String> skillDomains = SkillDomains.getSkillDomainsForRole(role);
        String keywordsStr = (missingKeywords == null || missingKeywords.isEmpty())
                ? "none"
                : String.join(", ", missingKeywords);
                
        return """
                You are a world-class ATS resume writer and senior recruiter specializing in the %s industry.

                TASK:
                Completely rewrite and upgrade the resume below for the role of %s in the %s industry.
                Use ONLY the information present in the resume below. Do NOT use or mix information from any other resume.
                Treat this as a completely fresh request with no memory of previous resumes.

                INDUSTRY-SPECIFIC REWRITING RULES FOR %s:
                - Personal Details: Extract ALL 7 fields ONLY from the resume below. Rules per field:
                  * name: full name exactly as written
                  * email: email address exactly as written
                  * phone: phone number exactly as written
                  * location: city and country (e.g. "Mumbai, India") — look for any city/country/address mention
                  * linkedin: full LinkedIn URL or username — look for "linkedin.com" anywhere in the resume
                  * github: full GitHub URL or username — look for "github.com" anywhere in the resume (if relevant to %s)
                  * portfolio: any other website/portfolio URL — look for personal websites, portfolio links
                  If a field is genuinely not present anywhere in the resume, set it to "". NEVER invent or reuse from previous resumes.
                - Professional Summary / Objective:
                  * First, determine if the person is a FRESHER (no work experience, only projects/internships) or EXPERIENCED (has at least one full-time job).
                  * Set "summaryType" to "PROFESSIONAL SUMMARY" if experienced, or "CAREER OBJECTIVE" if fresher.
                  * Write exactly 2–4 sentences covering: who they are, their key skills relevant to %s, what value they bring to %s industry, and their career goal.
                  * Be concise and impactful — no filler phrases like "I am a passionate professional".
                  * Use %s industry terminology and highlight relevant domain expertise.
                - Skills: Group into relevant domains from: %s.
                  * ONLY include skills that are relevant to the role of %s in %s industry. Do NOT list every skill from the resume.
                  * For Technical skills and Tools: write a sharp 1-line proficiency explanation per skill relevant to %s.
                  * For Soft Skills: set explanation to empty string "". List only 3-5 genuine soft skills relevant to %s industry.
                  * Omit any domain that has no relevant skills.
                  * Prioritize %s-specific skills and certifications.
                - Experience: Cover ALL roles — full-time jobs AND internships. For EACH role:
                  * Write 4-6 bullet points starting with strong action verbs (Engineered, Architected, Optimized, Delivered, Reduced, Increased, Built, Automated, Designed, Led, Managed, Implemented).
                  * Every bullet MUST focus on impact and outcome relevant to %s industry, not just tasks. Include metrics wherever possible.
                  * If the resume has no metrics, infer realistic ones based on %s industry context.
                  * Use %s industry terminology and highlight achievements relevant to the field.
                  * Separate each bullet point with a newline character \n.
                - Projects: For EACH project write 3-5 bullet points separated by newline character \n. Cover:
                  * What problem it solves in %s context
                  * Your specific contribution and implementation using %s methodologies
                  * Technologies/tools used and why they're relevant to %s
                  * Results or impact (quantify where possible using %s metrics)
                  Note: This section is critical for freshers — treat it with the same weight as experience.
                - Education: Keep all fields accurate. Extract degree, institution, year of passing, and CGPA/percentage exactly as written. Set cgpa to empty string if not present.
                  * Highlight education relevant to %s field.
                - Certifications: Extract ALL certifications, online courses, and professional certificates from the resume.
                  * Include name, issuer/platform, and year/date.
                  * Prioritize %s-relevant certifications and professional development.
                  * If certifications section is absent from the resume, return an empty array [].
                - Achievements: Extract ALL achievements, awards, hackathons, competitions, academic honours, and workplace recognition.
                  * Include title, a one-line description of what it was, and year.
                  * Highlight achievements relevant to %s industry.
                  * If none are present in the resume, return an empty array [].

                STRICT RULES:
                - ONLY use data from the resume provided below. Ignore all previous context.
                - Every section from the input MUST appear in the output.
                - Arrays must NEVER be empty.
                - Do NOT copy sentences verbatim from the input.
                - Do NOT invent companies, degrees, roles, or contact details.
                - Do NOT wrap response in markdown.
                - Return raw JSON only, starting with { and ending with }.
                - Use %s industry best practices and terminology throughout.

                SELECTED KEYWORDS TO INCORPORATE (these MUST appear naturally in the upgraded resume — in skills, experience bullets, project descriptions, or summary):
                %s

                If the keywords list is empty, use the resume content as-is without adding keywords.

                RESPOND IN THIS EXACT JSON STRUCTURE:
                {
                  "personalDetails": {
                    "name": "",
                    "email": "",
                    "phone": "",
                    "location": "",
                    "linkedin": "",
                    "github": "",
                    "portfolio": ""
                  },
                  "summaryType": "",
                  "professionalSummary": "",
                  "skillCategories": [
                    {
                      "domain": "",
                      "skills": [
                        { "name": "", "explanation": "" }
                      ]
                    }
                  ],
                  "experience": [
                    {
                      "role": "",
                      "company": "",
                      "duration": "",
                      "description": ""
                    }
                  ],
                  "projects": [
                    {
                      "name": "",
                      "description": "",
                      "techStack": []
                    }
                  ],
                  "education": [
                    {
                      "degree": "",
                      "institution": "",
                      "year": "",
                      "cgpa": ""
                    }
                  ],
                  "certifications": [
                    {
                      "name": "",
                      "issuer": "",
                      "year": ""
                    }
                  ],
                  "achievements": [
                    {
                      "title": "",
                      "description": "",
                      "year": ""
                    }
                  ]
                }

                RESUME TO UPGRADE (use ONLY this resume, ignore all previous context):
                <<<
                %s
                >>>
                """.formatted(industry, role, industry, industry, industry, industry, industry, industry, 
                            String.join(", ", skillDomains), role, industry, industry, industry, industry, 
                            industry, industry, industry, industry, industry, industry, industry, industry, 
                            industry, industry, industry, industry, keywordsStr, resumeText);
    }


    public UpgradedResumeDTO upgradeResume(
            String resumeText,
            List<String> missingKeywords,
            String role
    ) throws Exception {

        String prompt = buildResumeUpgradePrompt(resumeText, missingKeywords, role);

        String requestBody = buildRequestBody(prompt);

        String aiJson = getAIJson(requestBody);

        return objectMapper.readValue(aiJson, UpgradedResumeDTO.class);
    }


    private String getAIJson(String requestBody) throws Exception {

        String raw = callOpenAI(requestBody);

        String content = extractContentFromResponse(raw);

        content = content
                .replace("```json", "")
                .replace("```", "")
                .trim();

        return content;
    }



    private String callOpenAI(String requestBody) throws Exception {


        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .header("Authorization", "Bearer " + openAiConfig.getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody,
                        MediaType.parse("application/json")
                ))
                .build();


        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new RuntimeException("OpenAI call failed: " + response);
            }

            // ⭐ READ ONCE
            String responseBody = response.body().string();

            // ✅ Safe logging
            log.debug("OpenAI response: {}", responseBody);

            return responseBody;
        }
    }

    // For extracting Skills from Resume
    public String buildSkillExtractionPrompt(String resumeText, String role) {
        String industry = IndustryRoles.getIndustryForRole(role);
        List<String> allowedDomains = SkillDomains.getSkillDomainsForIndustry(industry);
        
        return """
You are an ATS-grade resume parser specialized in %s industry.

TASK:
Extract skills and return ONLY valid JSON.

RULES:
- Use ONLY the domains listed below that are relevant to %s
- DO NOT create new domains
- Each skill must have name and explanation (max 20 words)
- Focus on skills relevant to %s role
- No markdown
- No extra fields
- No commentary

ALLOWED DOMAINS:
%s

JSON FORMAT:
{
  "skillCategories": [
    {
      "domain": "string",
      "skills": [
        {
          "name": "string",
          "explanation": "string"
        }
      ]
    }
  ]
}

RESUME:
<<<
%s
>>>
""".formatted(industry, industry, role, String.join("\n- ", allowedDomains), resumeText);
    }


    public SkillsResponseDTO extractSkills(String resumeText, String role) throws Exception {
        String prompt = buildSkillExtractionPrompt(resumeText, role);

        String requestBody = buildRequestBody(prompt);
        String aiJson = getAIJson(requestBody);

        return objectMapper.readValue(aiJson, SkillsResponseDTO.class);
    }

    // Backward compatibility method
    public SkillsResponseDTO extractSkills(String resumeText) throws Exception {
        return extractSkills(resumeText, "Software Engineer"); // Default role
    }


    @Async("aiExecutor")
    public CompletableFuture<SkillsResponseDTO> extractSkillsAsync(String resumeText, String role) throws Exception{
        SkillsResponseDTO skills = extractSkills(resumeText, role);

        return CompletableFuture.completedFuture(skills);
    }

    // Backward compatibility method
    @Async("aiExecutor")
    public CompletableFuture<SkillsResponseDTO> extractSkillsAsync(String resumeText) throws Exception{
        return extractSkillsAsync(resumeText, "Software Engineer");
    }


    @Async
    public CompletableFuture<AtsScoreDTO> calculateAtsScoreAsync(String resumeText, String role) throws Exception {
        AtsScoreDTO atsScore = calculateAtsScore(resumeText, role);

        return CompletableFuture.completedFuture(atsScore);
    }

}
