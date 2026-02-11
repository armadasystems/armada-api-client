package ai.armada.client.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Slf4j
public class TokenRefreshScheduler {

    private final TokenProvider tokenProvider;

    public TokenRefreshScheduler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Scheduled job to refresh token at fixed intervals
     * Runs immediately on startup and then every refresh-rate-ms milliseconds
     * Checks if token has passed the threshold before refreshing
     */
    @Scheduled(fixedRateString = "${armada.api.tokenConfig.refresh-rate-ms}")
    public void scheduleTokenRefresh() {
        log.debug("Running scheduled token refresh check");
        
        try {
            if (tokenProvider.shouldRefreshToken()) {
                log.info("Token refresh threshold reached, initiating refresh");
                tokenProvider.forceRefresh();
            } else {
                AccessToken currentToken = tokenProvider.getCurrentToken();
                if (currentToken != null) {
                    log.debug("Token still valid. Remaining: {}s", currentToken.getRemainingSeconds());
                } else {
                    log.warn("No current token found, will attempt to obtain one on next access");
                }
            }
        } catch (Exception e) {
            log.error("Error during scheduled token refresh", e);
        }
    }
}