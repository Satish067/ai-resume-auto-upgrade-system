package com.ai.resume.service;

import com.ai.resume.ai.ResumeSchema;
import com.ai.resume.config.OpenAiConfig;
import com.ai.resume.constants.SkillDomains;
import com.ai.resume.dto.AtsScoreDTO;
import com.ai.resume.dto.ResumeSectionsDTO;
import com.ai.resume.dto.SkillsResponseDTO;
import com.ai.resume.dto.UpgradedResumeDTO;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ResumeAIService {


    private final OpenAiConfig openAiConfig;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, SkillsResponseDTO> skillsCache = new ConcurrentHashMap<>();

    private static final Logger log =
            LoggerFactory.getLogger(ResumeAIService.class);



    @PostConstruct
    public void checkApiKey() {
        System.out.println("OPENAI API KEY AT STARTUP = [" + openAiConfig.getApiKey() + "]");
        if (openAiConfig.getApiKey() == null || openAiConfig.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "❌ OpenAI API key not loaded. Check application.properties");
        }
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
        You are an expert technical recruiter.
        
        Extract the following sections from the resume:
        1. Skills
        2. Experience
        3. Projects
        4. Education
        
        Focus on a Java Developer role.
        
        Return STRICT JSON in this format:
        {
            "skills": [],
            "experience": [],
            "projects": [],
            "education": []
        }
        
        Resume:
        %s
        """.formatted(resumeText, ResumeSchema.UPGRADED_RESUME_SCHEMA);
    }
    private String buildRequestBody(String prompt) throws Exception{
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", openAiConfig.getModel());

        ArrayNode messages = root.putArray("messages");

        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", "you are an expert ATS resume analyzer.");

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
        return """
                You are an ATS (Applicant Tracking System) used by recruiters.
                
                Evaluate the resume for the role of %s.
                
                Scoring rules:
                - Score between 0 and 100
                - Consider keyword match, skills, experience and projects
                - Penalize missing core skills
                - Be strict and realistic
                
                Return STRICT JSON only in this format:
                {
                "score": 0,
                "missingKeywords": [],
                "improvementSuggestions": []
                }
                
                Resume:
                %s
                """.formatted(role, resumeText, ResumeSchema.UPGRADED_RESUME_SCHEMA, ResumeSchema.ATS_SCHEMA);
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
        return """
                You are an expert ATS resume writer and technical recruiter.
                
                TASK:
                Rewrite and upgrade the COMPLETE resume for the role of %s.
                
                IMPORTANT:
                Before responding, internally verify that ALL sections from the resume are present.
                If any section is missing, regenerate the response.
                
                
                MANDATORY RULES:
                
                - Rewrite EVERY section.
                - DO NOT copy sentences verbatim.
                - DO NOT return original text.
                - Improve impact, clarity, and ATS alignment.
                - Preserve factual accuracy.
                - Do NOT invent experience.
                - Do NOT wrap the response in markdown.
                - Return raw JSON only.
                
                CRITICAL:
                - All fields must be present.
                - Arrays must NEVER be empty.
                - Extract and rewrite ALL existing entries from the resume.
                - If information exists in the resume, it MUST appear in the output.
                
                If a section exists in the input, it MUST exist in the output.
                
                Never return only one section.
                
                Missing ATS Keywords to incorporate where relevant:
                %s
                
                
                Your response MUST start with { and end with }.
                Do not include explanations, markdown, or extra text.
                
                Do NOT summarize the resume.
                Rewrite it with equivalent or greater detail.
                
                
                {
                  "professionalSummary": "",
                  "skills": [
                    {
                      "domain": "Backend Development | Frontend | Databases | Cloud | Tools | Other",
                      "skills": [
                        {
                          "name": "Skill name",
                          "explanation": "1-line explanation of proficiency"
                        }
                      ]
                    }
                  ],
                  "experience": [
                    {
                      "role": "",
                      "company": "",
                      "duration": "",
                      "responsibilities": []
                    }
                  ],
                  "projects": [
                    {
                      "title": "",
                      "description": "",
                      "technologies": []
                    }
                  ],
                  "education": [
                    {
                      "degree": "",
                      "institution": "",
                      "year": ""
                    }
                  ]
                
                }
                
                RESUME:
                <<<
                %s
                >>>
                
                """.formatted(role, missingKeywords, resumeText);
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


    private String extractPureJson(String text) {

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }

        throw new RuntimeException("No JSON found in response: " + text);
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
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + openAiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
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
            log.error("🔥 REAL OPENAI RESPONSE:\n{}", responseBody);

            return responseBody;
        }
    }

    public String buildSkillExtractionPrompt(String resumeText) {
        return """
You are an ATS-grade resume parser.

TASK:
Extract skills and return ONLY valid JSON.

RULES:
- Use ONLY the domains listed below
- DO NOT create new domains
- Each skill must have name and explanation (max 20 words)
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
""".formatted(String.join("\n- ", SkillDomains.ALLOWED), resumeText);
    }

    public SkillsResponseDTO extractSkills(String resumeText) throws Exception {

        String prompt = buildSkillExtractionPrompt(resumeText);
        String key = Integer.toHexString(resumeText.hashCode());

        String requestBody = buildRequestBody(prompt);
        String aiJson = getAIJson(requestBody);

        return objectMapper.readValue(aiJson, SkillsResponseDTO.class);
    }

    @Async("aiExecutor")
    public CompletableFuture<SkillsResponseDTO> extractSkillsAsync(String resumeText) throws Exception{
        SkillsResponseDTO skills = extractSkills(resumeText);

        return CompletableFuture.completedFuture(skills);
    }

    @Async
    public CompletableFuture<AtsScoreDTO> calculateAtsScoreAsync(String resumeText, String role) throws Exception {
        AtsScoreDTO atsScore = calculateAtsScore(resumeText, role);

        return CompletableFuture.completedFuture(atsScore);
    }

}
