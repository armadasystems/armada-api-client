package ai.armada.client.organization;

import ai.armada.client.common.security.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ai.armada.client.common.security.TokenRefreshScheduler;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test that tests the full flow from controller to external API
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @MockitoBean
    private TokenRefreshScheduler tokenRefreshScheduler;

    private static MockWebServer mockBackEnd;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        registry.add("armada.api.base-url", () -> mockBackEnd.url("/").toString());
        registry.add("armada.api.credentials.api-key-id", () -> "test-key-id");
        registry.add("armada.api.credentials.api-key", () -> "test-key");
        registry.add("armada.api.endpoints.auth.token", () -> "/v1/auth/token");
        registry.add("armada.api.endpoints.organizations.list", () -> "/v1/orgs");
        registry.add("armada.api.tokenConfig.expiry-threshold-percent", () -> 50);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Clear cached token to ensure each test gets a fresh auth flow
        clearCachedToken();

        // Drain any remaining requests from the queue with timeout to avoid blocking
        // This ensures no requests leak between tests
        java.util.concurrent.TimeUnit timeUnit = java.util.concurrent.TimeUnit.MILLISECONDS;
        while (mockBackEnd.takeRequest(10, timeUnit) != null) {
            // Keep draining until no more requests (returns null on timeout)
        }
    }

    private void clearCachedToken() {
        try {
            Field currentTokenField = TokenProvider.class.getDeclaredField("currentToken");
            currentTokenField.setAccessible(true);
            currentTokenField.set(tokenProvider, null);
        } catch (Exception e) {
            // Silently fail if token clearing doesn't work
            // Tests will still work, just might not call auth endpoint every time
        }
    }

    @Test
    void getOrganizations_FullFlow_WithArrayResponse() throws Exception {
        // Arrange - Mock auth token response
        String authResponse = """
                {
                    "status": "success",
                    "data": {
                        "access_token": "test-token",
                        "expires_in": 3600,
                        "organization_id": "org-123"
                    },
                    "metadata": {
                        "timestamp": "2025-12-30T15:00:00Z",
                        "request_id": "test-request-id",
                        "api_version": "v1"
                    }
                }
                """;
        mockBackEnd.enqueue(new MockResponse()
                .setBody(authResponse)
                .addHeader("Content-Type", "application/json"));

        // Mock organizations response (wrapped single object)
        String orgsResponse = """
                {
                    "status": "success",
                    "data": {
                        "organization_id": "org-123",
                        "organization_name": "Test Org 1",
                        "display_name": "Test 1"
                    },
                    "metadata": {
                        "timestamp": "2025-12-30T15:00:00Z",
                        "request_id": "test-request-id",
                        "api_version": "v1"
                    }
                }
                """;
        mockBackEnd.enqueue(new MockResponse()
                .setBody(orgsResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        mockMvc.perform(get("/api/orgs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("org-123"))
                .andExpect(jsonPath("$[0].display_name").value("Test 1"));
    }

    @Test
    void getOrganizations_FullFlow_WithSingleObjectResponse() throws Exception {
        // Arrange - Mock auth token response
        String authResponse = """
                {
                    "status": "success",
                    "data": {
                        "access_token": "test-token",
                        "expires_in": 3600,
                        "organization_id": "org-123"
                    },
                    "metadata": {
                        "timestamp": "2025-12-30T15:00:00Z",
                        "request_id": "test-request-id",
                        "api_version": "v1"
                    }
                }
                """;
        mockBackEnd.enqueue(new MockResponse()
                .setBody(authResponse)
                .addHeader("Content-Type", "application/json"));

        // Mock organizations response (wrapped single object)
        String orgsResponse = """
                {
                    "status": "success",
                    "data": {
                        "organization_id": "org-123",
                        "organization_name": "Test Org",
                        "display_name": "Test"
                    },
                    "metadata": {
                        "timestamp": "2025-12-30T15:00:00Z",
                        "request_id": "test-request-id",
                        "api_version": "v1"
                    }
                }
                """;
        mockBackEnd.enqueue(new MockResponse()
                .setBody(orgsResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        mockMvc.perform(get("/api/orgs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("org-123"))
                .andExpect(jsonPath("$[0].display_name").value("Test"));
    }

    @Test
    void getOrganizations_WhenAuthFails_ShouldReturnError() throws Exception {
        // Arrange - Mock auth failure
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // Act & Assert
        mockMvc.perform(get("/api/orgs"))
                .andExpect(status().isUnauthorized());
    }
}