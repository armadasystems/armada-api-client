package ai.armada.client.common.security;

import ai.armada.client.config.ArmadaApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

    @Mock
    private AuthApiClient authApiClient;

    @Mock
    private ArmadaApiProperties properties;

    @Mock
    private ArmadaApiProperties.Token tokenConfig;

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(authApiClient, properties);
    }

    /**
     * Helper method to create a TokenResponse with the new nested structure
     */
    private TokenResponse createTokenResponse(String accessToken, long expiresIn, String organizationId) {
        TokenResponse.TokenData data = new TokenResponse.TokenData(accessToken, expiresIn, organizationId);
        TokenResponse.ResponseMetadata metadata = new TokenResponse.ResponseMetadata(
                "2025-12-30T20:30:57.520806151Z",
                "test-request-id",
                "v1"
        );
        return new TokenResponse("success", data, metadata);
    }

    @Test
    void getAccessToken_WhenNoToken_ShouldFetchNewToken() {
        // Arrange
        TokenResponse tokenResponse = createTokenResponse("test-access-token", 3600L, "org-123");
        when(authApiClient.fetchAuthToken()).thenReturn(tokenResponse);

        // Act
        String accessToken = tokenProvider.getAccessToken();

        // Assert
        assertNotNull(accessToken);
        assertEquals("test-access-token", accessToken);
        verify(authApiClient, times(1)).fetchAuthToken();
    }

    @Test
    void getAccessToken_WhenTokenValid_ShouldNotRefresh() {
        // Arrange
        TokenResponse tokenResponse = createTokenResponse("test-access-token", 3600L, "org-123");
        when(tokenConfig.getExpiryThresholdPercent()).thenReturn(50);
        when(properties.getTokenConfig()).thenReturn(tokenConfig);
        when(authApiClient.fetchAuthToken()).thenReturn(tokenResponse);

        // First call to get token
        tokenProvider.getAccessToken();

        // Act - Second call should use cached token
        String accessToken = tokenProvider.getAccessToken();

        // Assert
        assertNotNull(accessToken);
        assertEquals("test-access-token", accessToken);
        verify(authApiClient, times(1)).fetchAuthToken(); // Should only be called once
    }

    @Test
    void shouldRefreshToken_WhenNoToken_ShouldReturnTrue() {
        // Act
        boolean shouldRefresh = tokenProvider.shouldRefreshToken();

        // Assert
        assertTrue(shouldRefresh);
    }

    @Test
    void shouldRefreshToken_WhenTokenExpired_ShouldReturnTrue() {
        // Arrange - Create an expired token
        TokenResponse tokenResponse = createTokenResponse("expired-token", -100L, "org-123"); // Already expired
        when(authApiClient.fetchAuthToken()).thenReturn(tokenResponse);
        tokenProvider.getAccessToken(); // Initialize with expired token

        // Act
        boolean shouldRefresh = tokenProvider.shouldRefreshToken();

        // Assert
        assertTrue(shouldRefresh);
    }

    @Test
    void forceRefresh_ShouldRefreshToken() {
        // Arrange
        TokenResponse initialResponse = createTokenResponse("initial-token", 3600L, "org-123");
        TokenResponse refreshedResponse = createTokenResponse("refreshed-token", 3600L, "org-123");
        when(tokenConfig.getExpiryThresholdPercent()).thenReturn(50);
        when(properties.getTokenConfig()).thenReturn(tokenConfig);
        when(authApiClient.fetchAuthToken())
                .thenReturn(initialResponse)
                .thenReturn(refreshedResponse);

        tokenProvider.getAccessToken(); // Get initial token

        // Act
        tokenProvider.forceRefresh();

        // Assert
        String accessToken = tokenProvider.getAccessToken();
        assertEquals("refreshed-token", accessToken);
        verify(authApiClient, times(2)).fetchAuthToken();
    }

    @Test
    void getAccessToken_WhenAuthFails_ShouldThrowException() {
        // Arrange
        when(authApiClient.fetchAuthToken())
                .thenThrow(new AuthenticationException("Auth failed"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> 
                tokenProvider.getAccessToken());
    }

    @Test
    void getCurrentToken_ShouldReturnCurrentToken() {
        // Arrange
        TokenResponse tokenResponse = createTokenResponse("test-token", 3600L, "org-123");
        when(authApiClient.fetchAuthToken()).thenReturn(tokenResponse);
        tokenProvider.getAccessToken();

        // Act
        AccessToken currentToken = tokenProvider.getCurrentToken();

        // Assert
        assertNotNull(currentToken);
        assertEquals("test-token", currentToken.value());
    }
}