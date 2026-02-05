package com.progress.api.controller;

import com.progress.api.dto.LoginRequest;
import com.progress.api.dto.LoginResponse;
import com.progress.api.dto.LogoutResponse;
import com.progress.api.exception.ApiException;
import com.progress.api.security.JwtTokenProvider;
import com.progress.api.service.AuthService;
import com.progress.api.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60; // 30 days in seconds
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get JWT token")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse authResponse = authService.authenticate(request);
        
        // Set refresh token as httpOnly secure cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        
        // Return response WITHOUT refresh token in body (it's in the cookie)
        return ResponseEntity.ok(LoginResponse.builder()
                .token(authResponse.getToken())
                .uuid(authResponse.getUuid())
                .message(authResponse.getMessage())
                .refreshToken(null) // Don't send in response body
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get a new access token using refresh token from cookie")
    public ResponseEntity<LoginResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // Extract refresh token from httpOnly cookie
            String refreshToken = extractRefreshTokenFromCookie(request);
            
            if (refreshToken == null) {
                throw new ApiException("No refresh token provided", HttpStatus.UNAUTHORIZED);
            }
            
            // Validate refresh token
            if (!jwtTokenProvider.isTokenValid(refreshToken)) {
                clearRefreshTokenCookie(response);
                throw new ApiException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
            }
            
            // Check if refresh token is blacklisted
            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                clearRefreshTokenCookie(response);
                throw new ApiException("Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
            }
            
            // Extract claims from refresh token
            String uuid = jwtTokenProvider.extractUuid(refreshToken);
            String externalToken = jwtTokenProvider.extractExternalToken(refreshToken);
            
            // Generate new tokens
            String newAccessToken = jwtTokenProvider.generateToken(uuid, externalToken);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(uuid, externalToken);
            
            // Blacklist old refresh token (rotation for security)
            long oldExpiration = jwtTokenProvider.extractExpiration(refreshToken);
            tokenBlacklistService.blacklistToken(refreshToken, oldExpiration);
            
            // Set new refresh token as httpOnly cookie
            setRefreshTokenCookie(response, newRefreshToken);
            
            log.debug("Token refreshed successfully for uuid: {}", uuid);
            
            return ResponseEntity.ok(LoginResponse.builder()
                    .token(newAccessToken)
                    .uuid(uuid)
                    .message("Token refreshed successfully")
                    .refreshToken(null) // Don't send in response body
                    .build());
                    
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            clearRefreshTokenCookie(response);
            throw new ApiException("Failed to refresh token", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate the current JWT token and clear refresh cookie")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<LogoutResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        
        // Blacklist access token if provided
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                long expirationTimeMs = jwtTokenProvider.extractExpiration(token);
                tokenBlacklistService.blacklistToken(token, expirationTimeMs);
            } catch (Exception e) {
                log.warn("Could not blacklist access token: {}", e.getMessage());
            }
        }
        
        // Blacklist refresh token from cookie if present
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            try {
                long expirationTimeMs = jwtTokenProvider.extractExpiration(refreshToken);
                tokenBlacklistService.blacklistToken(refreshToken, expirationTimeMs);
            } catch (Exception e) {
                log.warn("Could not blacklist refresh token: {}", e.getMessage());
            }
        }
        
        // Clear the refresh token cookie
        clearRefreshTokenCookie(response);
        
        log.info("User logged out successfully");
        
        return ResponseEntity.ok(
            LogoutResponse.builder()
                .message("Logged out successfully")
                .success(true)
                .build()
        );
    }
    
    /**
     * Set refresh token as httpOnly, secure cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);  // JavaScript cannot access
        cookie.setSecure(true);     // Only sent over HTTPS
        cookie.setPath("/api/auth"); // Only sent to auth endpoints
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        cookie.setAttribute("SameSite", "None"); // Required for cross-origin cookies
        response.addCookie(cookie);
    }
    
    /**
     * Clear the refresh token cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0); // Delete cookie
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }
    
    /**
     * Extract refresh token from httpOnly cookie
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        
        return Arrays.stream(cookies)
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
