package com.progress.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progress.api.dto.RecommendationRequest;
import com.progress.api.dto.RecommendationResponse;
import com.progress.api.dto.RecommendationResponse.CurrentStatus;
import com.progress.api.dto.RecommendationResponse.Recommendation;
import com.progress.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Service for generating AI-powered major/speciality recommendations.
 * Analyzes student marks and suggests optimal academic paths.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final StudentService studentService;
    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        You are an expert academic advisor for the Algerian university system (LMD format).
        Your role is to analyze student academic performance and recommend the best majors/specialities.
        
        You will receive:
        1. Student's current academic information (field, major, level, grades)
        2. Available options for the next academic level
        3. Optional: Student's preferences and career interests
        
        Based on this information, provide personalized recommendations with:
        - Match scores (0-100) based on the student's strengths
        - Clear reasoning for each recommendation
        - Key subjects they'll study
        - Potential career outcomes
        
        Always respond in valid JSON format matching this structure:
        {
          "recommendations": [
            {
              "code": "option_code",
              "name": "French name",
              "nameAr": "Arabic name",
              "type": "major|speciality|master",
              "matchScore": 85,
              "reasoning": "Explanation of why this option suits the student",
              "keySubjects": ["subject1", "subject2"],
              "careerOutcomes": ["career1", "career2"],
              "furtherOptions": ["future_option_code"]
            }
          ],
          "summary": "Overall analysis summary in 2-3 sentences"
        }
        
        Prioritize options where the student has shown strong performance in related subjects.
        Be encouraging but realistic. Give honest assessments.
        """;

    /**
     * Generate recommendations for a student based on their academic data.
     *
     * @param uuid Student's UUID
     * @param externalToken Token for accessing PROGRES API
     * @param request Optional preferences from the student
     * @return AI-generated recommendations
     */
    public RecommendationResponse getRecommendations(
            String uuid,
            String externalToken,
            RecommendationRequest request
    ) {
        try {
            // 1. Fetch student data from PROGRES API
            Object studentDataRaw = studentService.getStudentData(uuid, externalToken);
            JsonNode studentData = objectMapper.valueToTree(studentDataRaw);
            
            // 2. Extract current status from student data (without university yet)
            CurrentStatus currentStatus = extractCurrentStatus(studentData);
            
            // 3. Get university name from student data for loading structure
            String universityName = null;
            if (studentData.isArray() && !studentData.isEmpty()) {
                universityName = getTextOrNull(studentData.get(0), "llEtablissementLatin");
            }
            
            // 4. Load academic structure based on university
            JsonNode academicStructure = loadAcademicStructure(universityName);
            
            // 5. Set university info in current status
            String[] universityInfo = getUniversityInfo(academicStructure);
            currentStatus.setUniversity(universityInfo[0]);
            currentStatus.setUniversityAr(universityInfo[1]);
            
            // 6. Find available options based on current level
            JsonNode availableOptions = findAvailableOptions(
                    academicStructure,
                    currentStatus.getField(),
                    currentStatus.getLevel()
            );
            
            // 7. Fetch exam data if available (for grade analysis)
            String examData = fetchExamDataSafe(uuid, studentData, externalToken);
            
            // 8. Build prompt for Groq
            String userPrompt = buildUserPrompt(currentStatus, availableOptions, examData, request);
            
            // 9. Call Groq API
            String aiResponse = groqClient.chat(SYSTEM_PROMPT, userPrompt);
            
            // 10. Parse and build response
            return parseAiResponse(aiResponse, currentStatus);
            
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating recommendations", e);
            throw new ApiException(
                    "Failed to generate recommendations: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private CurrentStatus extractCurrentStatus(JsonNode studentData) {
        // Student data is an array of registrations, get the most recent one
        if (studentData.isArray() && !studentData.isEmpty()) {
            JsonNode latestRegistration = studentData.get(0);
            
            return CurrentStatus.builder()
                    .field(getTextOrNull(latestRegistration, "llFiliere", "ofLlFiliere"))
                    .fieldAr(getTextOrNull(latestRegistration, "llFiliereArabe", "ofLlFiliereArabe"))
                    .major(getTextOrNull(latestRegistration, "ofLlFiliere"))
                    .majorAr(getTextOrNull(latestRegistration, "ofLlFiliereArabe"))
                    .speciality(getTextOrNull(latestRegistration, "ofLlSpecialite"))
                    .specialityAr(getTextOrNull(latestRegistration, "ofLlSpecialiteArabe"))
                    .level(getTextOrNull(latestRegistration, "refLibelleNiveau"))
                    .levelAr(getTextOrNull(latestRegistration, "refLibelleNiveauArabe"))
                    .currentAverage(latestRegistration.has("lastMoyenne") ? 
                            latestRegistration.get("lastMoyenne").asDouble() : null)
                    .academicYear(getTextOrNull(latestRegistration, "anneeAcademiqueCode"))
                    .build();
        }
        
        throw new ApiException("No academic registration found for student", HttpStatus.NOT_FOUND);
    }
    
    private String getTextOrNull(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                String text = node.get(fieldName).asText();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    /**
     * Normalizes a university name to create a key.
     * Example: "Université des Sciences et de la Technologie Houari Boumediène Alger"
     * becomes: "universite_des_sciences_et_de_la_technologie_houari_boumediene_alger"
     */
    private String normalizeUniversityName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s]", "")  // Remove special chars except spaces
                .trim()
                .replaceAll("\\s+", "_");        // Replace spaces with underscores
    }

    private JsonNode loadAcademicStructure(String universityName) throws IOException {
        ClassPathResource resource = new ClassPathResource("data/academic-structure.json");
        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode universities = root.path("universities");
            
            // If no university name provided, use first available
            if (universityName == null || universityName.isBlank()) {
                Map.Entry<String, JsonNode> first = universities.fields().next();
                log.info("No university specified, using default: {}", first.getValue().path("name").asText());
                return first.getValue();
            }
            
            // Normalize the input name to create a key
            String normalizedKey = normalizeUniversityName(universityName);
            log.info("Looking for university with key: {}", normalizedKey);
            
            // Direct key lookup
            JsonNode university = universities.path(normalizedKey);
            if (!university.isMissingNode()) {
                log.info("Found exact match for university: {}", university.path("name").asText());
                return university;
            }
            
            // If exact match fails, try partial matching on keys
            Iterator<Map.Entry<String, JsonNode>> fields = universities.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                
                // Check if key contains significant parts of input or vice versa
                if (key.contains(normalizedKey) || normalizedKey.contains(key)) {
                    log.info("Found partial match for university: {}", entry.getValue().path("name").asText());
                    return entry.getValue();
                }
            }
            
            // Fallback to first university if no match found
            Map.Entry<String, JsonNode> first = universities.fields().next();
            log.warn("No match found for '{}', using fallback: {}", universityName, first.getValue().path("name").asText());
            return first.getValue();
        }
    }
    
    /**
     * Get university info (name and Arabic name) from structure.
     */
    private String[] getUniversityInfo(JsonNode structure) {
        return new String[] {
            structure.path("name").asText("Unknown University"),
            structure.path("nameAr").asText("")
        };
    }

    private JsonNode findAvailableOptions(JsonNode structure, String currentField, String currentLevel) {
        // Find matching field in structure
        JsonNode fields = structure.path("fields");
        if (!fields.isArray()) {
            return objectMapper.createObjectNode();
        }
        
        for (JsonNode field : fields) {
            String fieldName = field.path("name").asText();
            if (currentField != null && currentField.toLowerCase().contains(fieldName.toLowerCase().substring(0, Math.min(3, fieldName.length())))) {
                // Found matching field, now find next level options
                return field.path("levels");
            }
        }
        
        // Return all fields if no match (for L1 students choosing a field)
        return fields;
    }

    private String fetchExamDataSafe(String uuid, JsonNode studentData, String externalToken) {
        try {
            if (studentData.isArray() && !studentData.isEmpty()) {
                long diaId = studentData.get(0).path("id").asLong();
                if (diaId > 0) {
                    Object examData = studentService.getExamData(uuid, String.valueOf(diaId), externalToken);
                    return objectMapper.writeValueAsString(examData);
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch exam data: {}", e.getMessage());
        }
        return "No detailed exam data available";
    }

    private String buildUserPrompt(
            CurrentStatus status,
            JsonNode availableOptions,
            String examData,
            RecommendationRequest request
    ) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("## Student's Current Academic Status\n");
        prompt.append("- Field: ").append(status.getField()).append("\n");
        prompt.append("- Level: ").append(status.getLevel()).append("\n");
        if (status.getMajor() != null) {
            prompt.append("- Major: ").append(status.getMajor()).append("\n");
        }
        if (status.getSpeciality() != null) {
            prompt.append("- Speciality: ").append(status.getSpeciality()).append("\n");
        }
        if (status.getCurrentAverage() != null) {
            prompt.append("- Current Average: ").append(status.getCurrentAverage()).append("/20\n");
        }
        prompt.append("- Academic Year: ").append(status.getAcademicYear()).append("\n\n");
        
        prompt.append("## Available Options for Next Level\n");
        prompt.append(availableOptions.toPrettyString()).append("\n\n");
        
        prompt.append("## Exam Data and Grades\n");
        prompt.append(examData).append("\n\n");
        
        if (request != null) {
            if (request.getCareerPreference() != null) {
                prompt.append("## Student's Career Preference\n");
                prompt.append(request.getCareerPreference()).append("\n\n");
            }
            if (request.getPreferredSubjects() != null && !request.getPreferredSubjects().isEmpty()) {
                prompt.append("## Preferred Subjects\n");
                prompt.append(String.join(", ", request.getPreferredSubjects())).append("\n\n");
            }
            if (request.getAdditionalContext() != null) {
                prompt.append("## Additional Context\n");
                prompt.append(request.getAdditionalContext()).append("\n\n");
            }
        }
        
        prompt.append("Based on this information, provide 3-5 personalized recommendations for the student's next academic step.");
        
        return prompt.toString();
    }

    private RecommendationResponse parseAiResponse(String aiResponse, CurrentStatus currentStatus) {
        try {
            JsonNode responseJson = objectMapper.readTree(aiResponse);
            
            List<Recommendation> recommendations = new ArrayList<>();
            JsonNode recsNode = responseJson.path("recommendations");
            
            if (recsNode.isArray()) {
                for (JsonNode rec : recsNode) {
                    recommendations.add(Recommendation.builder()
                            .code(rec.path("code").asText())
                            .name(rec.path("name").asText())
                            .nameAr(rec.path("nameAr").asText(""))
                            .type(rec.path("type").asText("speciality"))
                            .matchScore(rec.path("matchScore").asInt(0))
                            .reasoning(rec.path("reasoning").asText())
                            .keySubjects(jsonArrayToList(rec.path("keySubjects")))
                            .careerOutcomes(jsonArrayToList(rec.path("careerOutcomes")))
                            .furtherOptions(jsonArrayToList(rec.path("furtherOptions")))
                            .build());
                }
            }
            
            // Sort by matchScore descending, then by name ascending for stability
            recommendations.sort((a, b) -> {
                int scoreCompare = Integer.compare(b.getMatchScore(), a.getMatchScore());
                if (scoreCompare != 0) {
                    return scoreCompare;
                }
                // Secondary sort by name for consistent ordering when scores are equal
                return a.getName().compareToIgnoreCase(b.getName());
            });
            
            return RecommendationResponse.builder()
                    .currentStatus(currentStatus)
                    .recommendations(recommendations)
                    .summary(responseJson.path("summary").asText(""))
                    .model(groqClient.getModel())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", aiResponse, e);
            throw new ApiException("Failed to parse AI recommendation", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private List<String> jsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        }
        return list;
    }
}
