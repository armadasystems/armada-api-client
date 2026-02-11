package ai.armada.client.common.security;

import java.time.Instant;

// Access Token with expiration tracking
public record AccessToken(
        String value,
        Instant expiresAt,
        Instant issuedAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isExpiringSoon(int thresholdPercent) {
        if (thresholdPercent < 0 || thresholdPercent > 100) {
            throw new IllegalArgumentException("Threshold percent must be between 0 and 100");
        }
        
        long totalLifetime = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
        long threshold = (long) (totalLifetime * (thresholdPercent / 100.0));
        Instant thresholdTime = issuedAt.plusSeconds(threshold);
        
        return Instant.now().isAfter(thresholdTime);
    }

    public long getRemainingSeconds() {
        return expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
    }
}