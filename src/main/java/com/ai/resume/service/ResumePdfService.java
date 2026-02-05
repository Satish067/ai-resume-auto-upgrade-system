package com.ai.resume.service;

import com.ai.resume.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ResumePdfService {
    public byte[] generateResumePdf(UpgradedResumeDTO resume) throws Exception {

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        addSection(document, "PROFESSIONAL SUMMARY", resume.getProfessionalSummary(), headingFont, bodyFont);
        addSkillSection(document, resume.getSkillCategories(), headingFont, bodyFont);

        document.close();
        return out.toByteArray();
    }
    private void addEducationSection(
            Document document,
            String title,
            List<EducationDTO> educationList,
            Font headingFont,
            Font bodyFont
    ) throws Exception {

        document.add(new Paragraph(title, headingFont));

        for (EducationDTO edu : educationList) {

            String line = edu.getDegree()
                    + " - " + edu.getInstitution();

            if (edu.getYear() != null) {
                line += " (" + edu.getYear() + ")";
            }

            document.add(new Paragraph(line, bodyFont));
        }

        document.add(Chunk.NEWLINE);
    }

    private void addProjectsSection(
            Document document,
            String title,
            List<ProjectDTO> projects,
            Font headingFont,
            Font bodyFont
    ) throws Exception {

        document.add(new Paragraph(title, headingFont));

        for (ProjectDTO project : projects) {

            document.add(new Paragraph(
                    project.getName(),
                    bodyFont
            ));

            if (project.getDescription() != null) {
                document.add(new Paragraph(
                        project.getDescription(),
                        bodyFont
                ));
            }

            if (project.getTechStack() != null && !project.getTechStack().isEmpty()) {
                document.add(new Paragraph(
                        "Tech Stack: " + String.join(", ", project.getTechStack()),
                        bodyFont
                ));
            }

            document.add(Chunk.NEWLINE);
        }
    }

    private void addExperienceSection(
            Document document,
            String title,
            List<ExperienceDTO> experiences,
            Font headingFont,
            Font bodyFont
    ) throws Exception {

        document.add(new Paragraph(title, headingFont));

        for (ExperienceDTO exp : experiences) {
            document.add(new Paragraph(
                    exp.getRole() + " - " + exp.getCompany(),
                    bodyFont
            ));

            if (exp.getDescription() != null) {
                document.add(new Paragraph(
                        exp.getDescription(),
                        bodyFont
                ));
            }

            document.add(Chunk.NEWLINE);
        }
    }

    private void addSection(
            Document document,
            String title,
            String content,
            Font headingFont,
            Font bodyFont) throws Exception {

        document.add(new Paragraph(title, headingFont));
        document.add(new Paragraph(content, bodyFont));
        document.add(Chunk.NEWLINE);
    }

    private void addListSection(
            Document document,
            String title,
            List<String> items,
            Font headingFont,
            Font bodyFont) throws Exception {

        document.add(new Paragraph(title, headingFont));
        for (String item : items) {
            document.add(new Paragraph("- " + item, bodyFont));
        }
        document.add(Chunk.NEWLINE);
    }
    private void addSkillSection(
            Document document,
            List<SkillDomainDTO> domains,
            Font headingFont,
            Font bodyFont) throws Exception {

        if (domains == null || domains.isEmpty()) return;

        document.add(new Paragraph("SKILLS", headingFont));

        for (SkillDomainDTO domain : domains) {

            // Domain title
            document.add(new Paragraph(
                    "\n" + domain.getDomain(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)
            ));

            // Skills
            for (SkillItemDTO skill : domain.getSkills()) {

                document.add(new Paragraph(
                        "• " + skill.getName() +
                                " — " + skill.getExplanation(),
                        bodyFont
                ));
            }
        }
    }

}
