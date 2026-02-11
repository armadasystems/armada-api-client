package ai.armada.client.serviceline.client;

import ai.armada.client.common.security.TokenProvider;
import ai.armada.client.config.ArmadaApiProperties;
import ai.armada.client.serviceline.dto.*;
import ai.armada.client.serviceline.exception.ServiceLineApiException;
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

class ServiceLineApiClientTest {

    private MockWebServer mockWebServer;
    private ServiceLineApiClient serviceLineApiClient;
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
        organizations.setServiceLines("/v1/orgs/{orgId}/data-pools/{dataPoolId}/service-lines");
        organizations.setServiceLineById("/v1/orgs/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}");
        organizations.setServiceLineUsage("/v1/orgs/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/data-usage");
        organizations.setAllServiceLinesUsage("/v1/orgs/{orgId}/data-pools/{dataPoolId}/service-lines/data-usage");
        organizations.setServiceLineSettings("/v1/orgs/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/settings");
        organizations.setAllServiceLinesSettings("/v1/orgs/{orgId}/data-pools/{dataPoolId}/service-lines/settings");
        endpoints.setOrganizations(organizations);
        properties.setEndpoints(endpoints);

        WebClient webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();

        serviceLineApiClient = new ServiceLineApiClient(
                webClient, tokenProvider, properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchServiceLines_WhenArrayResponse_ShouldReturnList() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": [
                    {
                        "id": "sl-001",
                        "serviceLineName": "Service Line 1",
                        "serviceLineNumber": "555-0001",
                        "status": "Active",
                        "activationDate": "2024-01-01",
                        "kitNumbers": ["KIT001", "KIT002"]
                    },
                    {
                        "id": "sl-002",
                        "serviceLineName": "Service Line 2",
                        "serviceLineNumber": "555-0002",
                        "status": "Active",
                        "activationDate": "2024-02-01",
                        "kitNumbers": ["KIT003"]
                    }
                ],
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        List<ExternalServiceLineDto> serviceLines = serviceLineApiClient.fetchServiceLines(orgId, dataPoolId);

        // Assert
        assertNotNull(serviceLines);
        assertEquals(2, serviceLines.size());
        assertEquals("sl-001", serviceLines.get(0).id());
        assertEquals("Service Line 1", serviceLines.get(0).serviceLineName());
        assertEquals("555-0001", serviceLines.get(0).serviceLineNumber());
        assertEquals("sl-002", serviceLines.get(1).id());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools/dp-001/service-lines", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchServiceLines_WhenEmptyDataArray_ShouldReturnEmptyList() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": [],
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        List<ExternalServiceLineDto> serviceLines = serviceLineApiClient.fetchServiceLines(orgId, dataPoolId);

        // Assert
        assertNotNull(serviceLines);
        assertTrue(serviceLines.isEmpty());
    }

    @Test
    void fetchServiceLines_WhenNullData_ShouldReturnEmptyList() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": null,
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        List<ExternalServiceLineDto> serviceLines = serviceLineApiClient.fetchServiceLines(orgId, dataPoolId);

        // Assert
        assertNotNull(serviceLines);
        assertTrue(serviceLines.isEmpty());
    }

    @Test
    void fetchServiceLines_WhenErrorStatus_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "error",
                    "data": null,
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        ServiceLineApiException exception = assertThrows(
                ServiceLineApiException.class,
                () -> serviceLineApiClient.fetchServiceLines(orgId, dataPoolId));

        assertEquals("SERVICELINE_FETCH_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("error"));
    }

    @Test
    void fetchServiceLines_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineApiClient.fetchServiceLines(orgId, dataPoolId));
    }

    @Test
    void fetchServiceLines_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        ServiceLineApiException exception = assertThrows(
                ServiceLineApiException.class,
                () -> serviceLineApiClient.fetchServiceLines(orgId, dataPoolId));

        assertEquals("SERVICELINE_FETCH_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchServiceLineById_WhenSuccessful_ShouldReturnServiceLine() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                    "id": "sl-001",
                    "serviceLineName": "Service Line 1",
                    "serviceLineNumber": "555-0001",
                    "status": "Active",
                    "activationDate": "2024-01-01",
                    "kitNumbers": ["KIT001", "KIT002"]
                },
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        ExternalServiceLineDto serviceLine = serviceLineApiClient.fetchServiceLineById(orgId, dataPoolId, serviceLineId);

        // Assert
        assertNotNull(serviceLine);
        assertEquals("sl-001", serviceLine.id());
        assertEquals("Service Line 1", serviceLine.serviceLineName());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools/dp-001/service-lines/sl-001", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchServiceLineById_WhenNotFound_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-999";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineApiClient.fetchServiceLineById(orgId, dataPoolId, serviceLineId));
    }

    @Test
    void fetchServiceLineUsage_WhenSuccessful_ShouldReturnUsage() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        Integer billingCycles = 2;
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                    "serviceLineName": "Service Line 1",
                    "serviceLineNumber": "555-0001",
                    "status": "Active",
                    "activationDate": "2024-01-01",
                    "kitNumbers": ["KIT001"],
                    "billingCycles": []
                },
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        ServiceLineUsageDto usage = serviceLineApiClient.fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles);

        // Assert
        assertNotNull(usage);
        assertEquals("Service Line 1", usage.serviceLineName());
        assertNotNull(usage.billingCycles());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("/v1/orgs/org-123/data-pools/dp-001/service-lines/sl-001/data-usage"));
        assertTrue(recordedRequest.getPath().contains("billingCycles=2"));
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchServiceLineUsage_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        ServiceLineApiException exception = assertThrows(
                ServiceLineApiException.class,
                () -> serviceLineApiClient.fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, 1));

        assertEquals("SERVICELINE_USAGE_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchAllServiceLinesUsage_WhenSuccessful_ShouldReturnUsage() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 2;
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                    "id": "dp-001",
                    "serviceLines": []
                },
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        ServiceLinesUsageDto usage = serviceLineApiClient.fetchAllServiceLinesUsage(orgId, dataPoolId, billingCycles);

        // Assert
        assertNotNull(usage);
        assertEquals("dp-001", usage.id());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("/v1/orgs/org-123/data-pools/dp-001/service-lines/data-usage"));
        assertTrue(recordedRequest.getPath().contains("billingCycles=2"));
    }

    @Test
    void fetchAllServiceLinesUsage_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        ServiceLineApiException exception = assertThrows(
                ServiceLineApiException.class,
                () -> serviceLineApiClient.fetchAllServiceLinesUsage(orgId, dataPoolId, 1));

        assertEquals("SERVICELINE_USAGE_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchServiceLineSettings_WhenSuccessful_ShouldReturnSettings() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                    "serviceLineId": "sl-001",
                    "settings": [],
                    "notifications": []
                },
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        ServiceLineSettingsDto settings = serviceLineApiClient.fetchServiceLineSettings(orgId, dataPoolId, serviceLineId);

        // Assert
        assertNotNull(settings);
        assertEquals("sl-001", settings.serviceLineId());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools/dp-001/service-lines/sl-001/settings", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchServiceLineSettings_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        ServiceLineApiException exception = assertThrows(
                ServiceLineApiException.class,
                () -> serviceLineApiClient.fetchServiceLineSettings(orgId, dataPoolId, serviceLineId));

        assertEquals("SERVICELINE_SETTINGS_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchAllServiceLinesSettings_WhenSuccessful_ShouldReturnSettingsList() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": [
                    {
                        "serviceLineId": "sl-001",
                        "settings": [],
                        "notifications": []
                    },
                    {
                        "serviceLineId": "sl-002",
                        "settings": [],
                        "notifications": []
                    }
                ],
                    "metadata": {
                        "timestamp": "2024-01-01T00:00:00Z",
                        "request_id": "req-123",
                        "api_version": "1.0"
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        List<ServiceLineSettingsDto> settings = serviceLineApiClient.fetchAllServiceLinesSettings(orgId, dataPoolId);

        // Assert
        assertNotNull(settings);
        assertEquals(2, settings.size());
        assertEquals("sl-001", settings.get(0).serviceLineId());
        assertEquals("sl-002", settings.get(1).serviceLineId());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools/dp-001/service-lines/settings", recordedRequest.getPath());
    }

    @Test
    void fetchAllServiceLinesSettings_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        ServiceLineApiException exception = assertThrows(
                ServiceLineApiException.class,
                () -> serviceLineApiClient.fetchAllServiceLinesSettings(orgId, dataPoolId));

        assertEquals("SERVICELINE_SETTINGS_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchServiceLines_WhenTokenProviderFails_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(tokenProvider.getAccessToken())
                .thenThrow(new RuntimeException("Token error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineApiClient.fetchServiceLines(orgId, dataPoolId));
    }

    @Test
    void fetchServiceLines_WhenInvalidJson_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json {")
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineApiClient.fetchServiceLines(orgId, dataPoolId));
    }
}
