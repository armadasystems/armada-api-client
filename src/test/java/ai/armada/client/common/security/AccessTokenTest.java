package ai.armada.client.common.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenTest {

    @Test
    void isExpired_WhenTokenExpired_ShouldReturnTrue() {
        // Arrange
        Instant now = Instant.now();
        Instant expired = now.minusSeconds(100);
        AccessToken token = new AccessToken("test-token", expired, now.minusSeconds(3600));

        // Act & Assert
        assertTrue(token.isExpired());
    }

    @Test
    void isExpired_WhenTokenValid_ShouldReturnFalse() {
        // Arrange
        Instant now = Instant.now();
        Instant future = now.plusSeconds(3600);
        AccessToken token = new AccessToken("test-token", future, now);

        // Act & Assert
        assertFalse(token.isExpired());
    }

    @Test
    void isExpiringSoon_WhenPassedThreshold_ShouldReturnTrue() {
        // Arrange - Token issued 2 hours ago, expires in 1 hour (50% threshold passed)
        Instant issuedAt = Instant.now().minusSeconds(7200);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        AccessToken token = new AccessToken("test-token", expiresAt, issuedAt);

        // Act & Assert
        assertTrue(token.isExpiringSoon(50)); // 50% threshold
    }

    @Test
    void isExpiringSoon_WhenNotPassedThreshold_ShouldReturnFalse() {
        // Arrange - Token just issued, expires in 1 hour
        Instant issuedAt = Instant.now();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        AccessToken token = new AccessToken("test-token", expiresAt, issuedAt);

        // Act & Assert
        assertFalse(token.isExpiringSoon(50)); // 50% threshold not reached yet
    }

    @Test
    void isExpiringSoon_WithInvalidThreshold_ShouldThrowException() {
        // Arrange
        Instant now = Instant.now();
        AccessToken token = new AccessToken("test-token", now.plusSeconds(3600), now);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                token.isExpiringSoon(-1));
        assertThrows(IllegalArgumentException.class, () -> 
                token.isExpiringSoon(101));
    }

    @Test
    void getRemainingSeconds_ShouldReturnCorrectValue() {
        // Arrange
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(1800); // 30 minutes from now
        AccessToken token = new AccessToken("test-token", expiresAt, now);

        // Act
        long remaining = token.getRemainingSeconds();

        // Assert
        assertTrue(remaining >= 1790 && remaining <= 1810); // Allow for small timing variance
    }

    @Test
    void getRemainingSeconds_WhenExpired_ShouldReturnNegative() {
        // Arrange
        Instant now = Instant.now();
        Instant expiresAt = now.minusSeconds(100);
        AccessToken token = new AccessToken("test-token", expiresAt, now);

        // Act
        long remaining = token.getRemainingSeconds();

        // Assert
        assertTrue(remaining < 0);
    }

    @Test
    void value_ShouldReturnCorrectToken() {
        // Arrange
        String expectedToken = "my-test-token";
        AccessToken token = new AccessToken(
                expectedToken, 
                Instant.now().plusSeconds(3600), 
                Instant.now()
        );

        // Act & Assert
        assertEquals(expectedToken, token.value());
    }
}