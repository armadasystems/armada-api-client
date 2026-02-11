package ai.armada.client.organization.client;

import ai.armada.client.common.security.TokenProvider;
import ai.armada.client.config.ArmadaApiProperties;
import ai.armada.client.organization.dto.ExternalOrganizationDto;
import ai.armada.client.organization.exception.OrganizationApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationApiClientTest {

    private MockWebServer mockWebServer;
    private OrganizationApiClient organizationApiClient;
    private TokenProvider tokenProvider;
    private ArmadaApiProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Mock TokenProvider
        tokenProvider = mock(TokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("test-access-token");

        // Setup properties
        properties = new ArmadaApiProperties();
        properties.setBaseUrl(mockWebServer.url("/").toString());
        
        ArmadaApiProperties.Endpoints endpoints = new ArmadaApiProperties.Endpoints();
        ArmadaApiProperties.Endpoints.Organizations organizations = new ArmadaApiProperties.Endpoints.Organizations();
        organizations.setList("/v1/orgs");
        endpoints.setOrganizations(organizations);
        properties.setEndpoints(endpoints);

        WebClient webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();

        organizationApiClient = new OrganizationApiClient(
                webClient, tokenProvider, properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchOrganizations_WhenSuccessResponse_ShouldReturnList() throws InterruptedException {
        // Arrange - API returns nested response format
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                        "organization_id": "87284dde-9e87-4e25-90fd-131f10ab6f79",
                        "organization_name": "armada",
                        "display_name": "Armada Org"
                    },
                    "metadata": {
                        "timestamp": "2025-12-30T20:42:24.111586857Z",
                        "request_id": "f11fae8f-bfd6-4725-917f-8869e6c7f265",
                        "api_version": "v1"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        List<ExternalOrganizationDto> organizations = organizationApiClient.fetchOrganizations();

        // Assert
        assertNotNull(organizations);
        assertEquals(1, organizations.size());
        assertEquals("87284dde-9e87-4e25-90fd-131f10ab6f79", organizations.get(0).getId());
        assertEquals("armada", organizations.get(0).getName());
        assertEquals("Armada Org", organizations.get(0).getDisplayName());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchOrganizations_WhenErrorStatus_ShouldThrowException() {
        // Arrange - API returns error status
        String jsonResponse = """
                {
                    "status": "error",
                    "data": null,
                    "metadata": {
                        "timestamp": "2025-12-30T20:42:24.111586857Z",
                        "request_id": "f11fae8f-bfd6-4725-917f-8869e6c7f265",
                        "api_version": "v1"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        OrganizationApiException exception = assertThrows(
                OrganizationApiException.class,
                () -> organizationApiClient.fetchOrganizations());

        assertEquals("ORG_FETCH_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("error"));
    }

    @Test
    void fetchOrganizations_WhenNullData_ShouldReturnEmptyList() {
        // Arrange - API returns success but null data
        String jsonResponse = """
                {
                    "status": "success",
                    "data": null,
                    "metadata": {
                        "timestamp": "2025-12-30T20:42:24.111586857Z",
                        "request_id": "f11fae8f-bfd6-4725-917f-8869e6c7f265",
                        "api_version": "v1"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        List<ExternalOrganizationDto> organizations = organizationApiClient.fetchOrganizations();

        // Assert
        assertNotNull(organizations);
        assertTrue(organizations.isEmpty());
    }

    @Test
    void fetchOrganizations_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // Act & Assert
        assertThrows(OrganizationApiException.class, () -> 
                organizationApiClient.fetchOrganizations());
    }

    @Test
    void fetchOrganizations_WhenServerError_ShouldThrowException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        OrganizationApiException exception = assertThrows(
                OrganizationApiException.class, 
                () -> organizationApiClient.fetchOrganizations());
        
        assertEquals("ORG_FETCH_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchOrganizations_WhenInvalidJson_ShouldThrowException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json {")
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        assertThrows(OrganizationApiException.class, () -> 
                organizationApiClient.fetchOrganizations());
    }

    @Test
    void fetchOrganizations_WhenTokenProviderFails_ShouldThrowException() {
        // Arrange
        when(tokenProvider.getAccessToken())
                .thenThrow(new RuntimeException("Token error"));

        // Act & Assert
        assertThrows(OrganizationApiException.class, () -> 
                organizationApiClient.fetchOrganizations());
    }
}