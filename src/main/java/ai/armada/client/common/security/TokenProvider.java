package ai.armada.client.common.security;

import ai.armada.client.config.ArmadaApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class TokenProvider {

    private final AuthApiClient authApiClient;
    private final ArmadaApiProperties properties;
    private volatile AccessToken currentToken;

    public TokenProvider(AuthApiClient authApiClient, ArmadaApiProperties properties) {
        this.authApiClient = authApiClient;
        this.properties = properties;
    }

    /**
     * Gets the current access token, refreshing if necessary
     */
    public String getAccessToken() {
        if (shouldRefreshToken()) {
            synchronized (this) {
                if (shouldRefreshToken()) {
                    refreshToken();
                }
            }
        }
        
        if (currentToken == null) {
            throw new AuthenticationException("No valid access token available");
        }
        
        return currentToken.value();
    }

    /**
     * Force an immediate token refresh
     */
    public synchronized void forceRefresh() {
        log.info("Force refreshing access token");
        refreshToken();
    }

    /**
     * Check if token needs refresh based on threshold
     */
    public boolean shouldRefreshToken() {
        if (currentToken == null) {
            log.debug("No token exists, refresh needed");
            return true;
        }
        
        if (currentToken.isExpired()) {
            log.warn("Token has expired, refresh needed");
            return true;
        }
        
        int threshold = properties.getTokenConfig().getExpiryThresholdPercent();
        boolean expiringSoon = currentToken.isExpiringSoon(threshold);
        
        if (expiringSoon) {
            log.info("Token has passed {}% of its lifetime, refresh needed. Remaining: {}s", 
                    threshold, currentToken.getRemainingSeconds());
        }
        
        return expiringSoon;
    }

    /**
     * Get current token info for monitoring
     */
    public AccessToken getCurrentToken() {
        return currentToken;
    }

    /**
     * Refresh the access token
     */
    private void refreshToken() {
        try {
            log.info("Refreshing OAuth access token");
            
            TokenResponse response = authApiClient.fetchAuthToken();
            
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(response.getExpiresIn());

            currentToken = new AccessToken(
                    response.getAccessToken(),
                    expiresAt,
                    now
            );
            
            log.info("Access token refreshed successfully. Valid until: {}, Remaining: {}s", 
                    expiresAt, currentToken.getRemainingSeconds());
                    
        } catch (Exception e) {
            log.error("Failed to refresh access token", e);
            throw new AuthenticationException("Token refresh failed: " + e.getMessage(), e);
        }
    }
}