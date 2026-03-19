package com.ai.resume.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ResumeParserService {
    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    public String extractText(MultipartFile file) throws Exception {
        log.info("File name: {}, size: {}, content-type: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        String resumeText = "";
        String contentType = file.getContentType();

        if (contentType != null && contentType.equals("application/pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                resumeText = stripper.getText(document).trim();
            }
        } else {
            Tika tika = new Tika();
            tika.setMaxStringLength(-1);
            resumeText = tika.parseToString(file.getInputStream()).trim();
        }

        log.info("Extraction complete. Length = {}", resumeText.length());

        if (resumeText.isBlank()) {
            throw new RuntimeException("Could not extract text from file. Make sure it is a text-based PDF or DOCX, not a scanned image.");
        }

        return resumeText;
    }
}
