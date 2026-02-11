package ai.armada.client.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRefreshSchedulerTest {

    @Mock
    private TokenProvider tokenProvider;

    private TokenRefreshScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TokenRefreshScheduler(tokenProvider);
    }

    @Test
    void scheduleTokenRefresh_WhenTokenNeedsRefresh_ShouldRefresh() {
        // Arrange
        when(tokenProvider.shouldRefreshToken()).thenReturn(true);

        // Act
        scheduler.scheduleTokenRefresh();

        // Assert
        verify(tokenProvider, times(1)).shouldRefreshToken();
        verify(tokenProvider, times(1)).forceRefresh();
    }

    @Test
    void scheduleTokenRefresh_WhenTokenDoesNotNeedRefresh_ShouldNotRefresh() {
        // Arrange
        AccessToken validToken = new AccessToken(
                "test-token",
                Instant.now().plusSeconds(3600),
                Instant.now()
        );
        when(tokenProvider.shouldRefreshToken()).thenReturn(false);
        when(tokenProvider.getCurrentToken()).thenReturn(validToken);

        // Act
        scheduler.scheduleTokenRefresh();

        // Assert
        verify(tokenProvider, times(1)).shouldRefreshToken();
        verify(tokenProvider, never()).forceRefresh();
        verify(tokenProvider, times(1)).getCurrentToken();
    }

    @Test
    void scheduleTokenRefresh_WhenNoCurrentToken_ShouldHandleGracefully() {
        // Arrange
        when(tokenProvider.shouldRefreshToken()).thenReturn(false);
        when(tokenProvider.getCurrentToken()).thenReturn(null);

        // Act
        scheduler.scheduleTokenRefresh();

        // Assert
        verify(tokenProvider, times(1)).shouldRefreshToken();
        verify(tokenProvider, never()).forceRefresh();
    }

    @Test
    void scheduleTokenRefresh_WhenExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        when(tokenProvider.shouldRefreshToken())
                .thenThrow(new RuntimeException("Test exception"));

        // Act - Should not throw exception
        scheduler.scheduleTokenRefresh();

        // Assert
        verify(tokenProvider, times(1)).shouldRefreshToken();
        verify(tokenProvider, never()).forceRefresh();
    }

    @Test
    void scheduleTokenRefresh_WhenRefreshFails_ShouldHandleGracefully() {
        // Arrange
        when(tokenProvider.shouldRefreshToken()).thenReturn(true);
        doThrow(new RuntimeException("Refresh failed"))
                .when(tokenProvider).forceRefresh();

        // Act - Should not throw exception
        scheduler.scheduleTokenRefresh();

        // Assert
        verify(tokenProvider, times(1)).shouldRefreshToken();
        verify(tokenProvider, times(1)).forceRefresh();
    }
}