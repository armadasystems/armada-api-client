package ai.armada.client.common.security;

import ai.armada.client.config.ArmadaApiProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AuthApiClientTest {

    private MockWebServer mockWebServer;
    private AuthApiClient authApiClient;
    private ArmadaApiProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Setup properties
        properties = new ArmadaApiProperties();
        properties.setBaseUrl(mockWebServer.url("/").toString());
        
        ArmadaApiProperties.Endpoints endpoints = new ArmadaApiProperties.Endpoints();
        ArmadaApiProperties.Endpoints.Auth auth = new ArmadaApiProperties.Endpoints.Auth();
        auth.setToken("/v1/auth/token");
        endpoints.setAuth(auth);
        properties.setEndpoints(endpoints);
        
        ArmadaApiProperties.Credentials credentials = new ArmadaApiProperties.Credentials();
        credentials.setApiKeyId("test-key-id");
        credentials.setApiKey("test-key");
        properties.setCredentials(credentials);

        WebClient webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
        
        authApiClient = new AuthApiClient(webClient, properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchAuthToken_WhenSuccessful_ShouldReturnToken() throws InterruptedException {
        // Arrange
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                        "access_token": "test-access-token",
                        "expires_in": 3600,
                        "organization_id": "org-123"
                    },
                    "metadata": {
                        "timestamp": "2025-12-30T20:30:57.520806151Z",
                        "request_id": "test-request-id",
                        "api_version": "v1"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        TokenResponse response = authApiClient.fetchAuthToken();

        // Assert
        assertNotNull(response);
        assertEquals("success", response.status());
        assertEquals("test-access-token", response.getAccessToken());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals("org-123", response.getOrganizationId());
        assertTrue(response.isSuccess());

        // Verify the request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/v1/auth/token", recordedRequest.getPath());
        assertTrue(recordedRequest.getBody().readUtf8().contains("test-key-id"));
    }

    @Test
    void fetchAuthToken_WhenUnsuccessful_ShouldThrowException() {
        // Arrange
        String jsonResponse = """
                {
                    "status": "error",
                    "data": null,
                    "metadata": {
                        "timestamp": "2025-12-30T20:30:57.520806151Z",
                        "request_id": "test-request-id",
                        "api_version": "v1"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () ->
                authApiClient.fetchAuthToken());
    }

    @Test
    void fetchAuthToken_WhenServerError_ShouldThrowException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> 
                authApiClient.fetchAuthToken());
    }

    @Test
    void fetchAuthToken_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> 
                authApiClient.fetchAuthToken());
    }

    @Test
    void fetchAuthToken_WhenNetworkError_ShouldThrowException() throws IOException {
        // Arrange
        mockWebServer.shutdown(); // Simulate network error

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> 
                authApiClient.fetchAuthToken());
    }
}