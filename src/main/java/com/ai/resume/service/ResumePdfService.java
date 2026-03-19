package com.ai.resume.service;

import com.ai.resume.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumePdfService {

    private static final Color ACCENT        = new Color(30, 64, 103);
    private static final Color SECTION_LINE  = new Color(30, 64, 103);
    private static final Color TEXT_DARK     = new Color(30, 30, 30);
    private static final Color TEXT_MUTED    = new Color(90, 90, 90);

    public byte[] generateResumePdf(UpgradedResumeDTO resume) throws Exception {
        Document document = new Document(PageSize.A4, 45, 45, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        addPersonalDetails(document, resume.getPersonalDetails());
        addProfessionalSummary(document, resume.getSummaryType(), resume.getProfessionalSummary());
        addSkillsSection(document, resume.getSkillCategories());
        addExperienceSection(document, resume.getExperience());
        addProjectsSection(document, resume.getProjects());
        addEducationSection(document, resume.getEducation());
        addCertificationsSection(document, resume.getCertifications());
        addAchievementsSection(document, resume.getAchievements());

        document.close();
        return out.toByteArray();
    }

    private void addPersonalDetails(Document document, PersonalDetailsDTO details) throws Exception {
        if (details == null) return;

        Font nameFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, ACCENT);
        Font contactFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_MUTED);
        Font linkFont    = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, new Color(30, 64, 103));

        // Full name
        if (details.getName() != null && !details.getName().isBlank()) {
            Paragraph name = new Paragraph(details.getName(), nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingAfter(5);
            document.add(name);
        }

        // Row 1: phone | email | location
        List<String> row1 = new java.util.ArrayList<>();
        if (details.getPhone()    != null && !details.getPhone().isBlank())    row1.add(details.getPhone());
        if (details.getEmail()    != null && !details.getEmail().isBlank())    row1.add(details.getEmail());
        if (details.getLocation() != null && !details.getLocation().isBlank()) row1.add(details.getLocation());
        if (!row1.isEmpty()) {
            Paragraph r1 = new Paragraph(String.join("  ·  ", row1), contactFont);
            r1.setAlignment(Element.ALIGN_CENTER);
            r1.setSpacingAfter(3);
            document.add(r1);
        }

        // Row 2: linkedin | github | portfolio
        List<String> row2 = new java.util.ArrayList<>();
        if (details.getLinkedin()  != null && !details.getLinkedin().isBlank())  row2.add(details.getLinkedin());
        if (details.getGithub()    != null && !details.getGithub().isBlank())    row2.add(details.getGithub());
        if (details.getPortfolio() != null && !details.getPortfolio().isBlank()) row2.add(details.getPortfolio());
        if (!row2.isEmpty()) {
            Paragraph r2 = new Paragraph(String.join("  ·  ", row2), linkFont);
            r2.setAlignment(Element.ALIGN_CENTER);
            r2.setSpacingAfter(14);
            document.add(r2);
        }
    }

    private void addProfessionalSummary(Document document, String summaryType, String summary) throws Exception {
        if (summary == null || summary.isBlank()) return;

        document.add(Chunk.NEWLINE);
        String label = (summaryType != null && !summaryType.isBlank()) ? summaryType.toUpperCase() : "PROFESSIONAL SUMMARY";
        addSectionTitle(document, label);

        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);
        Paragraph p = new Paragraph(summary, bodyFont);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setLeading(16);
        p.setSpacingAfter(10);
        document.add(p);
    }

    private void addSkillsSection(Document document, List<SkillDomainDTO> domains) throws Exception {
        if (domains == null || domains.isEmpty()) return;

        addSectionTitle(document, "SKILLS");

        Font domainFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, ACCENT);
        Font skillName  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, TEXT_DARK);
        Font skillDesc  = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_MUTED);
        Font tagFont    = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_DARK);

        for (SkillDomainDTO domain : domains) {
            if (domain.getSkills() == null || domain.getSkills().isEmpty()) continue;

            Paragraph domainTitle = new Paragraph(domain.getDomain().toUpperCase(), domainFont);
            domainTitle.setSpacingBefore(7);
            domainTitle.setSpacingAfter(3);
            document.add(domainTitle);

            boolean isSoftSkills = domain.getDomain().equalsIgnoreCase("Soft Skills");

            if (isSoftSkills) {
                // Render as comma-separated tags on one line
                String tags = domain.getSkills().stream()
                        .map(SkillItemDTO::getName)
                        .collect(Collectors.joining("  •  "));
                Paragraph tagLine = new Paragraph(tags, tagFont);
                tagLine.setSpacingAfter(3);
                document.add(tagLine);
            } else {
                for (SkillItemDTO skill : domain.getSkills()) {
                    Paragraph skillLine = new Paragraph();
                    skillLine.add(new Chunk("• " + skill.getName(), skillName));
                    if (skill.getExplanation() != null && !skill.getExplanation().isBlank()) {
                        skillLine.add(new Chunk("  —  " + skill.getExplanation(), skillDesc));
                    }
                    skillLine.setSpacingAfter(2);
                    document.add(skillLine);
                }
            }
        }
        document.add(Chunk.NEWLINE);
    }

    private void addExperienceSection(Document document, List<ExperienceDTO> experiences) throws Exception {
        if (experiences == null || experiences.isEmpty()) return;

        addSectionTitle(document, "WORK EXPERIENCE");

        Font titleFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK);
        Font companyFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, ACCENT);
        Font durationFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10.5f, TEXT_MUTED);
        Font bulletFont   = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_DARK);

        for (ExperienceDTO exp : experiences) {
            // Job Title  |  Company Name on same line
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{55, 45});
            header.setSpacingBefore(10);

            PdfPCell titleCell = new PdfPCell(new Phrase(exp.getRole(), titleFont));
            titleCell.setBorder(0);
            titleCell.setPaddingBottom(2);

            PdfPCell companyCell = new PdfPCell(new Phrase(exp.getCompany(), companyFont));
            companyCell.setBorder(0);
            companyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            companyCell.setPaddingBottom(2);

            header.addCell(titleCell);
            header.addCell(companyCell);
            document.add(header);

            // Duration below
            if (exp.getDuration() != null && !exp.getDuration().isBlank()) {
                Paragraph duration = new Paragraph(exp.getDuration(), durationFont);
                duration.setSpacingAfter(4);
                document.add(duration);
            }

            // Bullet points
            if (exp.getDescription() != null) {
                for (String bullet : exp.getDescription().split("\\n|\u2022")) {
                    String trimmed = bullet.trim();
                    if (!trimmed.isBlank()) {
                        Paragraph bulletPara = new Paragraph("•  " + trimmed, bulletFont);
                        bulletPara.setIndentationLeft(10);
                        bulletPara.setFirstLineIndent(-10);
                        bulletPara.setLeading(15);
                        bulletPara.setSpacingAfter(3);
                        document.add(bulletPara);
                    }
                }
            }
        }
        document.add(Chunk.NEWLINE);
    }

    private void addProjectsSection(Document document, List<ProjectDTO> projects) throws Exception {
        if (projects == null || projects.isEmpty()) return;

        addSectionTitle(document, "PROJECTS");

        Font nameFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK);
        Font stackFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, ACCENT);
        Font bulletFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_DARK);

        for (ProjectDTO project : projects) {
            // Project title left | Tech stack right
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{50, 50});
            header.setSpacingBefore(10);
            header.setKeepTogether(true);

            PdfPCell nameCell = new PdfPCell(new Phrase(project.getName(), nameFont));
            nameCell.setBorder(0);
            nameCell.setPaddingBottom(2);

            String stack = (project.getTechStack() != null && !project.getTechStack().isEmpty())
                    ? String.join(" • ", project.getTechStack()) : "";
            PdfPCell stackCell = new PdfPCell(new Phrase(stack, stackFont));
            stackCell.setBorder(0);
            stackCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            stackCell.setPaddingBottom(2);

            header.addCell(nameCell);
            header.addCell(stackCell);
            document.add(header);

            // Description as bullet points
            if (project.getDescription() != null) {
                for (String bullet : project.getDescription().split("\\n|•")) {
                    String trimmed = bullet.trim();
                    if (!trimmed.isBlank()) {
                        Paragraph bulletPara = new Paragraph("•  " + trimmed, bulletFont);
                        bulletPara.setIndentationLeft(10);
                        bulletPara.setFirstLineIndent(-10);
                        bulletPara.setLeading(15);
                        bulletPara.setSpacingAfter(3);
                        document.add(bulletPara);
                    }
                }
            }
        }
        document.add(Chunk.NEWLINE);
    }

    private void addEducationSection(Document document, List<EducationDTO> educationList) throws Exception {
        if (educationList == null || educationList.isEmpty()) return;

        addSectionTitle(document, "EDUCATION");

        Font degreeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK);
        Font yearFont   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10.5f, TEXT_MUTED);
        Font infoFont   = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_MUTED);
        Font cgpaFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, ACCENT);

        for (EducationDTO edu : educationList) {
            // Degree left | Year right
            PdfPTable row1 = new PdfPTable(2);
            row1.setWidthPercentage(100);
            row1.setWidths(new float[]{70, 30});
            row1.setSpacingBefore(10);

            PdfPCell degreeCell = new PdfPCell(new Phrase(edu.getDegree(), degreeFont));
            degreeCell.setBorder(0);
            degreeCell.setPaddingBottom(2);

            PdfPCell yearCell = new PdfPCell(new Phrase(edu.getYear() != null ? edu.getYear() : "", yearFont));
            yearCell.setBorder(0);
            yearCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            yearCell.setPaddingBottom(2);

            row1.addCell(degreeCell);
            row1.addCell(yearCell);
            document.add(row1);

            // Institution left | CGPA right
            PdfPTable row2 = new PdfPTable(2);
            row2.setWidthPercentage(100);
            row2.setWidths(new float[]{70, 30});

            PdfPCell instCell = new PdfPCell(new Phrase(edu.getInstitution() != null ? edu.getInstitution() : "", infoFont));
            instCell.setBorder(0);
            instCell.setPaddingBottom(4);

            String cgpaText = (edu.getCgpa() != null && !edu.getCgpa().isBlank()) ? "CGPA: " + edu.getCgpa() : "";
            PdfPCell cgpaCell = new PdfPCell(new Phrase(cgpaText, cgpaFont));
            cgpaCell.setBorder(0);
            cgpaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cgpaCell.setPaddingBottom(4);

            row2.addCell(instCell);
            row2.addCell(cgpaCell);
            document.add(row2);
        }
    }

    private void addAchievementsSection(Document document, List<AchievementDTO> achievements) throws Exception {
        if (achievements == null || achievements.isEmpty()) return;

        addSectionTitle(document, "ACHIEVEMENTS & AWARDS");

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_DARK);
        Font descFont  = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_MUTED);
        Font yearFont  = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10.5f, TEXT_MUTED);

        for (AchievementDTO ach : achievements) {
            PdfPTable row = new PdfPTable(2);
            row.setWidthPercentage(100);
            row.setWidths(new float[]{75, 25});
            row.setSpacingBefore(8);

            // Title + description stacked on the left
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(0);
            leftCell.setPaddingBottom(2);
            leftCell.addElement(new Phrase(ach.getTitle() != null ? ach.getTitle() : "", titleFont));
            if (ach.getDescription() != null && !ach.getDescription().isBlank()) {
                leftCell.addElement(new Phrase(ach.getDescription(), descFont));
            }

            // Year on the right
            PdfPCell rightCell = new PdfPCell(new Phrase(ach.getYear() != null ? ach.getYear() : "", yearFont));
            rightCell.setBorder(0);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            rightCell.setPaddingBottom(2);

            row.addCell(leftCell);
            row.addCell(rightCell);
            document.add(row);
        }
        document.add(Chunk.NEWLINE);
    }

    private void addCertificationsSection(Document document, List<CertificationDTO> certifications) throws Exception {
        if (certifications == null || certifications.isEmpty()) return;

        addSectionTitle(document, "CERTIFICATIONS");

        Font nameFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_DARK);
        Font issuerFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_MUTED);
        Font yearFont   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10.5f, TEXT_MUTED);

        for (CertificationDTO cert : certifications) {
            PdfPTable row = new PdfPTable(2);
            row.setWidthPercentage(100);
            row.setWidths(new float[]{75, 25});
            row.setSpacingBefore(8);

            // Cert name + issuer stacked on the left
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(0);
            leftCell.setPaddingBottom(2);
            leftCell.addElement(new Phrase(cert.getName() != null ? cert.getName() : "", nameFont));
            if (cert.getIssuer() != null && !cert.getIssuer().isBlank()) {
                leftCell.addElement(new Phrase(cert.getIssuer(), issuerFont));
            }

            // Year on the right
            PdfPCell rightCell = new PdfPCell(new Phrase(cert.getYear() != null ? cert.getYear() : "", yearFont));
            rightCell.setBorder(0);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            rightCell.setPaddingBottom(2);

            row.addCell(leftCell);
            row.addCell(rightCell);
            document.add(row);
        }
        document.add(Chunk.NEWLINE);
    }

    private void addSectionTitle(Document document, String title) throws Exception {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, ACCENT);

        // Wrap title + divider together so they never split across pages
        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);
        block.setKeepTogether(true);
        block.setSpacingBefore(10);

        PdfPCell titleCell = new PdfPCell(new Phrase(title, titleFont));
        titleCell.setBorder(0);
        titleCell.setBorderWidthBottom(1.5f);
        titleCell.setBorderColorBottom(SECTION_LINE);
        titleCell.setPaddingBottom(4);
        titleCell.setPaddingTop(0);
        block.addCell(titleCell);

        document.add(block);
    }
}
