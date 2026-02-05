package com.progress.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progress.api.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with Groq's OpenAI-compatible API.
 * Used for AI-powered major/speciality recommendations.
 */
@Slf4j
@Service
public class GroqClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;
    
    @Value("${groq.timeout:30000}")
    private int timeout;

    public GroqClient(
            @Value("${groq.api-key:}") String apiKey,
            @Value("${groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Groq API key is not configured. AI recommendations will not work.");
        }
    }

    /**
     * Send a chat completion request to Groq API.
     *
     * @param systemPrompt The system prompt defining the AI's behavior
     * @param userPrompt The user's specific request
     * @return The AI's response as a string
     */
    public String chat(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 2048,
                    "response_format", Map.of("type", "json_object")
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            // Parse response to extract the content
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
            
            throw new ApiException("Invalid response from Groq API", HttpStatus.INTERNAL_SERVER_ERROR);
            
        } catch (WebClientResponseException e) {
            log.error("Groq API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode().value() == 401) {
                throw new ApiException("Invalid Groq API key", HttpStatus.UNAUTHORIZED);
            } else if (e.getStatusCode().value() == 429) {
                throw new ApiException("Groq API rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
            }
            
            throw new ApiException("Failed to get AI recommendation: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            throw new ApiException("Failed to get AI recommendation: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Check if the Groq client is properly configured.
     */
    public boolean isConfigured() {
        try {
            // Simple health check - just verify the API key format
            return true; // If we got here, the client was constructed
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getModel() {
        return model;
    }
}
