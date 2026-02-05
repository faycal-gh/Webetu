package com.progress.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for getting AI-powered major/speciality recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    /**
     * Optional: Student's preferences for career direction.
     * Examples: "software development", "research", "industry"
     */
    private String careerPreference;
    
    /**
     * Optional: Subjects the student enjoys or excels at.
     * Examples: ["mathematics", "physics", "programming"]
     */
    private List<String> preferredSubjects;
    
    /**
     * Optional: University code if student wants recommendations for a specific university.
     * If null, uses "default" structure.
     */
    private String universityCode;
    
    /**
     * Optional: Additional context the student wants the AI to consider.
     */
    private String additionalContext;
}
