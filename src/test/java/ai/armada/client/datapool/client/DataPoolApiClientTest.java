package ai.armada.client.datapool.client;

import ai.armada.client.common.security.TokenProvider;
import ai.armada.client.config.ArmadaApiProperties;
import ai.armada.client.datapool.dto.*;
import ai.armada.client.datapool.exception.DataPoolApiException;
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

class DataPoolApiClientTest {

    private MockWebServer mockWebServer;
    private DataPoolApiClient dataPoolApiClient;
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
        organizations.setDataPools("/v1/orgs/{orgId}/data-pools");
        organizations.setDataPoolById("/v1/orgs/{orgId}/data-pools/{dataPoolId}");
        organizations.setDataPoolUsage("/v1/orgs/{orgId}/data-pools/{dataPoolId}/data-usage");
        organizations.setDataPoolSettings("/v1/orgs/{orgId}/data-pools/{dataPoolId}/settings");
        endpoints.setOrganizations(organizations);
        properties.setEndpoints(endpoints);

        WebClient webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();

        dataPoolApiClient = new DataPoolApiClient(
                webClient, tokenProvider, properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchDataPools_WhenSuccessResponse_ShouldReturnList() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": [
                        {
                            "id": "dp-001",
                            "name": "Data Pool 1",
                            "country": "USA",
                            "planType": "Premium",
                            "status": "Active",
                            "startDate": "2024-01-01",
                            "endDate": "2024-12-31",
                            "dataAvailableGB": 100.0,
                            "dataUsedGB": 50.0,
                            "totalServiceLines": 5
                        },
                        {
                            "id": "dp-002",
                            "name": "Data Pool 2",
                            "country": "CAN",
                            "planType": "Standard",
                            "status": "Active",
                            "startDate": "2024-02-01",
                            "endDate": "2024-12-31",
                            "dataAvailableGB": 200.0,
                            "dataUsedGB": 75.0,
                            "totalServiceLines": 10
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
        List<ExternalDataPoolDto> dataPools = dataPoolApiClient.fetchDataPools(orgId);

        // Assert
        assertNotNull(dataPools);
        assertEquals(2, dataPools.size());
        assertEquals("dp-001", dataPools.get(0).id());
        assertEquals("Data Pool 1", dataPools.get(0).name());
        assertEquals("USA", dataPools.get(0).country());
        assertEquals("dp-002", dataPools.get(1).id());
        assertEquals("Data Pool 2", dataPools.get(1).name());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchDataPools_WhenEmptyDataArray_ShouldReturnEmptyList() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
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
        List<ExternalDataPoolDto> dataPools = dataPoolApiClient.fetchDataPools(orgId);

        // Assert
        assertNotNull(dataPools);
        assertTrue(dataPools.isEmpty());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchDataPools_WhenNullData_ShouldReturnEmptyList() {
        // Arrange
        String orgId = "org-123";
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
        List<ExternalDataPoolDto> dataPools = dataPoolApiClient.fetchDataPools(orgId);

        // Assert
        assertNotNull(dataPools);
        assertTrue(dataPools.isEmpty());
    }

    @Test
    void fetchDataPools_WhenErrorStatus_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
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
        DataPoolApiException exception = assertThrows(
                DataPoolApiException.class,
                () -> dataPoolApiClient.fetchDataPools(orgId));

        assertEquals("DATAPOOL_FETCH_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("error"));
    }

    @Test
    void fetchDataPools_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolApiClient.fetchDataPools(orgId));
    }

    @Test
    void fetchDataPools_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        DataPoolApiException exception = assertThrows(
                DataPoolApiException.class,
                () -> dataPoolApiClient.fetchDataPools(orgId));

        assertEquals("DATAPOOL_FETCH_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchDataPools_WhenInvalidJson_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json {")
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolApiClient.fetchDataPools(orgId));
    }

    @Test
    void fetchDataPools_WhenTokenProviderFails_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        when(tokenProvider.getAccessToken())
                .thenThrow(new RuntimeException("Token error"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolApiClient.fetchDataPools(orgId));
    }

    @Test
    void fetchDataPoolById_WhenSuccessful_ShouldReturnDataPool() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
                        "id": "dp-001",
                        "name": "Data Pool 1",
                        "country": "USA",
                        "planType": "Premium",
                        "status": "Active",
                        "startDate": "2024-01-01",
                        "endDate": "2024-12-31",
                        "dataAvailableGB": 100.0,
                        "dataUsedGB": 50.0,
                        "totalServiceLines": 5
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
        ExternalDataPoolDto dataPool = dataPoolApiClient.fetchDataPoolById(orgId, dataPoolId);

        // Assert
        assertNotNull(dataPool);
        assertEquals("dp-001", dataPool.id());
        assertEquals("Data Pool 1", dataPool.name());
        assertEquals("USA", dataPool.country());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools/dp-001", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchDataPoolById_WhenNotFound_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-999";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolApiClient.fetchDataPoolById(orgId, dataPoolId));
    }

    @Test
    void fetchDataPoolUsage_WhenSuccessful_ShouldReturnUsage() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 2;
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
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
        DataPoolDataUsageDto usage = dataPoolApiClient.fetchDataPoolUsage(orgId, dataPoolId, billingCycles);

        // Assert
        assertNotNull(usage);
        assertNotNull(usage.billingCycles());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("/v1/orgs/org-123/data-pools/dp-001/data-usage"));
        assertTrue(recordedRequest.getPath().contains("billingCycles=2"));
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchDataPoolUsage_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        DataPoolApiException exception = assertThrows(
                DataPoolApiException.class,
                () -> dataPoolApiClient.fetchDataPoolUsage(orgId, dataPoolId, 1));

        assertEquals("DATAPOOL_USAGE_ERROR", exception.getErrorCode());
    }

    @Test
    void fetchDataPoolSettings_WhenSuccessful_ShouldReturnSettings() throws InterruptedException {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String jsonResponse = """
                {
                    "status": "success",
                    "data": {
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
        DataPoolSettingsDto settings = dataPoolApiClient.fetchDataPoolSettings(orgId, dataPoolId);

        // Assert
        assertNotNull(settings);
        assertNotNull(settings.settings());
        assertNotNull(settings.notifications());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1/orgs/org-123/data-pools/dp-001/settings", recordedRequest.getPath());
        assertEquals("Bearer test-access-token", recordedRequest.getHeader("Authorization"));
    }

    @Test
    void fetchDataPoolSettings_WhenServerError_ShouldThrowException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        DataPoolApiException exception = assertThrows(
                DataPoolApiException.class,
                () -> dataPoolApiClient.fetchDataPoolSettings(orgId, dataPoolId));

        assertEquals("DATAPOOL_SETTINGS_ERROR", exception.getErrorCode());
    }
}
