package com.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationDTO {
    @JsonAlias({"name", "title", "certification", "course"})
    private String name;
    @JsonAlias({"issuer", "provider", "platform", "issuedBy", "organization"})
    private String issuer;
    @JsonAlias({"year", "date", "issuedOn", "completedOn"})
    private String year;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}
