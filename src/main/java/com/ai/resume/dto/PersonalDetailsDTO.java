package com.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalDetailsDTO {
    private String name;
    private String email;
    private String phone;
    @JsonAlias({"city", "address", "cityCountry"})
    private String location;
    @JsonAlias({"linkedinUrl", "linkedIn", "linkedInUrl", "linkedin_url"})
    private String linkedin;
    @JsonAlias({"githubUrl", "gitHub", "gitHubUrl", "github_url"})
    private String github;
    @JsonAlias({"portfolioUrl", "portfolioLink", "website", "portfolio_url"})
    private String portfolio;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }
}
