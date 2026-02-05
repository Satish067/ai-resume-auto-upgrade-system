package com.ai.resume.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ResumeParserService {
    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);
    public String extractText(MultipartFile file) throws Exception {

        Tika tika = new Tika();

        String resumeText = tika.parseToString(file.getInputStream());

        log.error("🔥 EXTRACTION COMPLETE 🔥 Length = {}", resumeText.length());

        return resumeText;
    }

}
