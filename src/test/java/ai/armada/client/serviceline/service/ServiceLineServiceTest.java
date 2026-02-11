package ai.armada.client.serviceline.service;

import ai.armada.client.serviceline.client.ServiceLineApiClient;
import ai.armada.client.serviceline.dto.*;
import ai.armada.client.serviceline.exception.ServiceLineApiException;
import ai.armada.client.serviceline.mapper.ServiceLineMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceLineServiceTest {

    @Mock
    private ServiceLineApiClient apiClient;

    @Mock
    private ServiceLineMapper mapper;

    private ServiceLineService serviceLineService;

    @BeforeEach
    void setUp() {
        serviceLineService = new ServiceLineService(apiClient, mapper);
    }

    @Test
    void getServiceLines_WhenSuccessful_ShouldReturnMappedServiceLines() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        ExternalServiceLineDto externalSL1 = new ExternalServiceLineDto(
                "sl-001",
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001", "KIT002")
        );

        ExternalServiceLineDto externalSL2 = new ExternalServiceLineDto(
                "sl-002",
                "Service Line 2",
                "555-0002",
                "Active",
                LocalDate.of(2024, 2, 1),
                List.of("KIT003")
        );

        List<ExternalServiceLineDto> externalServiceLines = List.of(externalSL1, externalSL2);

        ServiceLineDto mappedSL1 = new ServiceLineDto(
                "sl-001",
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001", "KIT002")
        );

        ServiceLineDto mappedSL2 = new ServiceLineDto(
                "sl-002",
                "Service Line 2",
                "555-0002",
                "Active",
                LocalDate.of(2024, 2, 1),
                List.of("KIT003")
        );

        when(apiClient.fetchServiceLines(orgId, dataPoolId)).thenReturn(externalServiceLines);
        when(mapper.toDto(externalSL1)).thenReturn(mappedSL1);
        when(mapper.toDto(externalSL2)).thenReturn(mappedSL2);

        // Act
        List<ServiceLineDto> result = serviceLineService.getServiceLines(orgId, dataPoolId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("sl-001", result.get(0).id());
        assertEquals("Service Line 1", result.get(0).serviceLineName());
        assertEquals("sl-002", result.get(1).id());
        assertEquals("Service Line 2", result.get(1).serviceLineName());

        verify(apiClient, times(1)).fetchServiceLines(orgId, dataPoolId);
        verify(mapper, times(2)).toDto(any(ExternalServiceLineDto.class));
    }

    @Test
    void getServiceLines_WhenEmptyList_ShouldReturnEmptyList() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(apiClient.fetchServiceLines(orgId, dataPoolId)).thenReturn(new ArrayList<>());

        // Act
        List<ServiceLineDto> result = serviceLineService.getServiceLines(orgId, dataPoolId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(apiClient, times(1)).fetchServiceLines(orgId, dataPoolId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getServiceLines_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(apiClient.fetchServiceLines(orgId, dataPoolId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_FETCH_ERROR", "API Error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineService.getServiceLines(orgId, dataPoolId));

        verify(apiClient, times(1)).fetchServiceLines(orgId, dataPoolId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getServiceLineById_WhenSuccessful_ShouldReturnMappedServiceLine() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";

        ExternalServiceLineDto externalSL = new ExternalServiceLineDto(
                "sl-001",
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001", "KIT002")
        );

        ServiceLineDto mappedSL = new ServiceLineDto(
                "sl-001",
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001", "KIT002")
        );

        when(apiClient.fetchServiceLineById(orgId, dataPoolId, serviceLineId)).thenReturn(externalSL);
        when(mapper.toDto(externalSL)).thenReturn(mappedSL);

        // Act
        ServiceLineDto result = serviceLineService.getServiceLineById(orgId, dataPoolId, serviceLineId);

        // Assert
        assertNotNull(result);
        assertEquals("sl-001", result.id());
        assertEquals("Service Line 1", result.serviceLineName());
        assertEquals("555-0001", result.serviceLineNumber());

        verify(apiClient, times(1)).fetchServiceLineById(orgId, dataPoolId, serviceLineId);
        verify(mapper, times(1)).toDto(externalSL);
    }

    @Test
    void getServiceLineById_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";

        when(apiClient.fetchServiceLineById(orgId, dataPoolId, serviceLineId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_FETCH_ERROR", "API Error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineService.getServiceLineById(orgId, dataPoolId, serviceLineId));

        verify(apiClient, times(1)).fetchServiceLineById(orgId, dataPoolId, serviceLineId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getServiceLineUsage_WhenSuccessful_ShouldReturnUsage() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        Integer billingCycles = 2;

        ServiceLineUsageDto expectedUsage = new ServiceLineUsageDto(
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001"),
                List.of()
        );

        when(apiClient.fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles))
                .thenReturn(expectedUsage);

        // Act
        ServiceLineUsageDto result = serviceLineService.getServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles);

        // Assert
        assertNotNull(result);
        assertEquals("Service Line 1", result.serviceLineName());
        assertNotNull(result.billingCycles());

        verify(apiClient, times(1)).fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles);
    }

    @Test
    void getServiceLineUsage_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        Integer billingCycles = 1;

        when(apiClient.fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles))
                .thenThrow(new ServiceLineApiException("SERVICELINE_USAGE_ERROR", "API Error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineService.getServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles));

        verify(apiClient, times(1)).fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles);
    }

    @Test
    void getAllServiceLinesUsage_WhenSuccessful_ShouldReturnUsage() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 2;

        ServiceLinesUsageDto expectedUsage = new ServiceLinesUsageDto(
                "dp-001",
                List.of()
        );

        when(apiClient.fetchAllServiceLinesUsage(orgId, dataPoolId, billingCycles))
                .thenReturn(expectedUsage);

        // Act
        ServiceLinesUsageDto result = serviceLineService.getAllServiceLinesUsage(orgId, dataPoolId, billingCycles);

        // Assert
        assertNotNull(result);
        assertEquals("dp-001", result.id());
        assertNotNull(result.serviceLines());

        verify(apiClient, times(1)).fetchAllServiceLinesUsage(orgId, dataPoolId, billingCycles);
    }

    @Test
    void getAllServiceLinesUsage_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 1;

        when(apiClient.fetchAllServiceLinesUsage(orgId, dataPoolId, billingCycles))
                .thenThrow(new ServiceLineApiException("SERVICELINE_USAGE_ERROR", "API Error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineService.getAllServiceLinesUsage(orgId, dataPoolId, billingCycles));

        verify(apiClient, times(1)).fetchAllServiceLinesUsage(orgId, dataPoolId, billingCycles);
    }

    @Test
    void getServiceLineSettings_WhenSuccessful_ShouldReturnSettings() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";

        ServiceLineSettingsDto expectedSettings = new ServiceLineSettingsDto(
                "sl-001",
                List.of(),
                List.of()
        );

        when(apiClient.fetchServiceLineSettings(orgId, dataPoolId, serviceLineId))
                .thenReturn(expectedSettings);

        // Act
        ServiceLineSettingsDto result = serviceLineService.getServiceLineSettings(orgId, dataPoolId, serviceLineId);

        // Assert
        assertNotNull(result);
        assertEquals("sl-001", result.serviceLineId());
        assertNotNull(result.settings());
        assertNotNull(result.notifications());

        verify(apiClient, times(1)).fetchServiceLineSettings(orgId, dataPoolId, serviceLineId);
    }

    @Test
    void getServiceLineSettings_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";

        when(apiClient.fetchServiceLineSettings(orgId, dataPoolId, serviceLineId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_SETTINGS_ERROR", "API Error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineService.getServiceLineSettings(orgId, dataPoolId, serviceLineId));

        verify(apiClient, times(1)).fetchServiceLineSettings(orgId, dataPoolId, serviceLineId);
    }

    @Test
    void getAllServiceLinesSettings_WhenSuccessful_ShouldReturnSettingsList() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        ServiceLineSettingsDto settings1 = new ServiceLineSettingsDto(
                "sl-001",
                List.of(),
                List.of()
        );

        ServiceLineSettingsDto settings2 = new ServiceLineSettingsDto(
                "sl-002",
                List.of(),
                List.of()
        );

        List<ServiceLineSettingsDto> expectedSettings = List.of(settings1, settings2);

        when(apiClient.fetchAllServiceLinesSettings(orgId, dataPoolId))
                .thenReturn(expectedSettings);

        // Act
        List<ServiceLineSettingsDto> result = serviceLineService.getAllServiceLinesSettings(orgId, dataPoolId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("sl-001", result.get(0).serviceLineId());
        assertEquals("sl-002", result.get(1).serviceLineId());

        verify(apiClient, times(1)).fetchAllServiceLinesSettings(orgId, dataPoolId);
    }

    @Test
    void getAllServiceLinesSettings_WhenEmptyList_ShouldReturnEmptyList() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        when(apiClient.fetchAllServiceLinesSettings(orgId, dataPoolId))
                .thenReturn(List.of());

        // Act
        List<ServiceLineSettingsDto> result = serviceLineService.getAllServiceLinesSettings(orgId, dataPoolId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(apiClient, times(1)).fetchAllServiceLinesSettings(orgId, dataPoolId);
    }

    @Test
    void getAllServiceLinesSettings_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        when(apiClient.fetchAllServiceLinesSettings(orgId, dataPoolId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_SETTINGS_ERROR", "API Error"));

        // Act & Assert
        assertThrows(ServiceLineApiException.class, () ->
                serviceLineService.getAllServiceLinesSettings(orgId, dataPoolId));

        verify(apiClient, times(1)).fetchAllServiceLinesSettings(orgId, dataPoolId);
    }
}
