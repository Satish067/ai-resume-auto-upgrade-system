package com.ai.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AchievementDTO {
    @JsonAlias({"title", "name", "award", "achievement"})
    private String title;
    @JsonAlias({"description", "details", "summary"})
    private String description;
    @JsonAlias({"year", "date"})
    private String year;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}
