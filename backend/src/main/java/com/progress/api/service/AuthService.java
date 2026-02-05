package com.progress.api.service;

import com.progress.api.dto.ExternalAuthResponse;
import com.progress.api.dto.LoginRequest;
import com.progress.api.dto.LoginResponse;
import com.progress.api.exception.ApiException;
import com.progress.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            ExternalAuthResponse externalResponse = webClient.post()
                    .uri("/authentication/v1/")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ExternalAuthResponse.class)
                    .block();

            if (externalResponse == null || externalResponse.getToken() == null) {
                throw new ApiException("Authentication failed: Invalid response from server", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = jwtTokenProvider.generateToken(
                    externalResponse.getUuid(),
                    externalResponse.getToken()
            );

            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    externalResponse.getUuid(),
                    externalResponse.getToken()
            );

            return LoginResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .uuid(externalResponse.getUuid())
                    .message("Authentication successful")
                    .build();

        } catch (WebClientResponseException e) {
            log.error("External API error: {}", e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ApiException("اسم المستخدم أو كلمة المرور غير صحيحة", HttpStatus.UNAUTHORIZED);
            }
            
            throw new ApiException(
                    "Authentication failed: " + e.getStatusText(),
                    HttpStatus.valueOf(e.getStatusCode().value())
            );
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication error", e);
            throw new ApiException("Authentication failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
