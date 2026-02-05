package com.ai.resume.ai;

public final class ResumeSchema {

    private ResumeSchema() {}

    public static final String UPGRADED_RESUME_SCHEMA = """
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
                
                """;

    public static final String ATS_SCHEMA = """
Return ONLY valid JSON in this exact structure:

{
  "score": number,
  "missingKeywords": ["string"],
  "improvementSuggestions": ["string"]
}
""";
}
