package com.progress.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing blacklisted JWT tokens.
 * Tokens are stored in-memory with their expiration times.
 * Expired tokens are automatically cleaned up by a scheduled task.
 * 
 * Note: For distributed deployments, consider using Redis instead.
 */
@Slf4j
@Service
public class TokenBlacklistService {

    // Map of token -> expiration time (epoch milliseconds)
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist.
     * 
     * @param token The JWT token to blacklist
     * @param expirationTimeMs The token's expiration time in milliseconds since epoch
     */
    public void blacklistToken(String token, long expirationTimeMs) {
        if (token == null || token.isBlank()) {
            log.warn("Attempted to blacklist null or blank token");
            return;
        }
        blacklistedTokens.put(token, expirationTimeMs);
        log.debug("Token blacklisted, expiration: {}", Instant.ofEpochMilli(expirationTimeMs));
    }

    /**
     * Check if a token is blacklisted.
     * 
     * @param token The JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Remove a token from the blacklist.
     * Used primarily for testing purposes.
     * 
     * @param token The token to remove
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }

    /**
     * Get the current size of the blacklist.
     * Used primarily for monitoring/testing.
     * 
     * @return Number of blacklisted tokens
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Scheduled task to clean up expired tokens from the blacklist.
     * Runs every 5 minutes to prevent memory buildup.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int beforeSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        
        int removed = beforeSize - blacklistedTokens.size();
        if (removed > 0) {
            log.info("Cleaned up {} expired tokens from blacklist. Remaining: {}", 
                    removed, blacklistedTokens.size());
        }
    }

    /**
     * Clear all tokens from the blacklist.
     * Used primarily for testing purposes.
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }
}
