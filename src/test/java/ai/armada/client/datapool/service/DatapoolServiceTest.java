package ai.armada.client.datapool.service;

import ai.armada.client.datapool.client.DataPoolApiClient;
import ai.armada.client.datapool.dto.*;
import ai.armada.client.datapool.exception.DataPoolApiException;
import ai.armada.client.datapool.mapper.DataPoolMapper;
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
class DatapoolServiceTest {

    @Mock
    private DataPoolApiClient apiClient;

    @Mock
    private DataPoolMapper mapper;

    private DataPoolService dataPoolService;

    @BeforeEach
    void setUp() {
        dataPoolService = new DataPoolService(apiClient, mapper);
    }

    @Test
    void getDataPools_WhenSuccessful_ShouldReturnMappedDataPools() {
        // Arrange
        String orgId = "org-123";

        ExternalDataPoolDto externalDataPool1 = new ExternalDataPoolDto(
                "dp-001",
                "Data Pool 1",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                100.0f,
                50.0f,
                5
        );

        ExternalDataPoolDto externalDataPool2 = new ExternalDataPoolDto(
                "dp-002",
                "Data Pool 2",
                "CAN",
                "Standard",
                "Active",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 12, 31),
                200.0f,
                75.0f,
                10
        );

        List<ExternalDataPoolDto> externalDataPools = List.of(externalDataPool1, externalDataPool2);

        DataPoolDto mappedDataPool1 = new DataPoolDto(
                "dp-001",
                "Data Pool 1",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                100.0f,
                50.0f,
                5
        );

        DataPoolDto mappedDataPool2 = new DataPoolDto(
                "dp-002",
                "Data Pool 2",
                "CAN",
                "Standard",
                "Active",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 12, 31),
                200.0f,
                75.0f,
                10
        );

        when(apiClient.fetchDataPools(orgId)).thenReturn(externalDataPools);
        when(mapper.toDto(externalDataPool1)).thenReturn(mappedDataPool1);
        when(mapper.toDto(externalDataPool2)).thenReturn(mappedDataPool2);

        // Act
        List<DataPoolDto> result = dataPoolService.getDataPools(orgId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("dp-001", result.get(0).id());
        assertEquals("Data Pool 1", result.get(0).name());
        assertEquals("dp-002", result.get(1).id());
        assertEquals("Data Pool 2", result.get(1).name());

        verify(apiClient, times(1)).fetchDataPools(orgId);
        verify(mapper, times(2)).toDto(any(ExternalDataPoolDto.class));
    }

    @Test
    void getDataPools_WhenEmptyList_ShouldReturnEmptyList() {
        // Arrange
        String orgId = "org-123";
        when(apiClient.fetchDataPools(orgId)).thenReturn(new ArrayList<>());

        // Act
        List<DataPoolDto> result = dataPoolService.getDataPools(orgId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(apiClient, times(1)).fetchDataPools(orgId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getDataPools_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        when(apiClient.fetchDataPools(orgId))
                .thenThrow(new DataPoolApiException("DATAPOOL_FETCH_ERROR", "API Error"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolService.getDataPools(orgId));

        verify(apiClient, times(1)).fetchDataPools(orgId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getDataPools_WhenMapperThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        ExternalDataPoolDto externalDataPool = new ExternalDataPoolDto(
                "dp-001",
                "Data Pool 1",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                100.0f,
                50.0f,
                5
        );

        when(apiClient.fetchDataPools(orgId)).thenReturn(List.of(externalDataPool));
        when(mapper.toDto(externalDataPool)).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                dataPoolService.getDataPools(orgId));
    }

    @Test
    void getDataPoolById_WhenSuccessful_ShouldReturnMappedDataPool() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        ExternalDataPoolDto externalDataPool = new ExternalDataPoolDto(
                "dp-001",
                "Data Pool 1",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                100.0f,
                50.0f,
                5
        );

        DataPoolDto mappedDataPool = new DataPoolDto(
                "dp-001",
                "Data Pool 1",
                "USA",
                "Premium",
                "Active",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                100.0f,
                50.0f,
                5
        );

        when(apiClient.fetchDataPoolById(orgId, dataPoolId)).thenReturn(externalDataPool);
        when(mapper.toDto(externalDataPool)).thenReturn(mappedDataPool);

        // Act
        DataPoolDto result = dataPoolService.getDataPoolById(orgId, dataPoolId);

        // Assert
        assertNotNull(result);
        assertEquals("dp-001", result.id());
        assertEquals("Data Pool 1", result.name());
        assertEquals("USA", result.country());

        verify(apiClient, times(1)).fetchDataPoolById(orgId, dataPoolId);
        verify(mapper, times(1)).toDto(externalDataPool);
    }

    @Test
    void getDataPoolById_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        when(apiClient.fetchDataPoolById(orgId, dataPoolId))
                .thenThrow(new DataPoolApiException("DATAPOOL_FETCH_ERROR", "API Error"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolService.getDataPoolById(orgId, dataPoolId));

        verify(apiClient, times(1)).fetchDataPoolById(orgId, dataPoolId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getDataPoolUsage_WhenSuccessful_ShouldReturnUsage() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 2;

        DataPoolDataUsageDto expectedUsage = new DataPoolDataUsageDto(
                List.of()
        );

        when(apiClient.fetchDataPoolUsage(orgId, dataPoolId, billingCycles))
                .thenReturn(expectedUsage);

        // Act
        DataPoolDataUsageDto result = dataPoolService.getDataPoolUsage(orgId, dataPoolId, billingCycles);

        // Assert
        assertNotNull(result);
        assertNotNull(result.billingCycles());

        verify(apiClient, times(1)).fetchDataPoolUsage(orgId, dataPoolId, billingCycles);
    }

    @Test
    void getDataPoolUsage_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 1;

        when(apiClient.fetchDataPoolUsage(orgId, dataPoolId, billingCycles))
                .thenThrow(new DataPoolApiException("DATAPOOL_USAGE_ERROR", "API Error"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolService.getDataPoolUsage(orgId, dataPoolId, billingCycles));

        verify(apiClient, times(1)).fetchDataPoolUsage(orgId, dataPoolId, billingCycles);
    }

    @Test
    void getDataPoolSettings_WhenSuccessful_ShouldReturnSettings() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        DataPoolSettingsDto expectedSettings = new DataPoolSettingsDto(
                List.of(),
                List.of()
        );

        when(apiClient.fetchDataPoolSettings(orgId, dataPoolId))
                .thenReturn(expectedSettings);

        // Act
        DataPoolSettingsDto result = dataPoolService.getDataPoolSettings(orgId, dataPoolId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.settings());
        assertNotNull(result.notifications());

        verify(apiClient, times(1)).fetchDataPoolSettings(orgId, dataPoolId);
    }

    @Test
    void getDataPoolSettings_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        when(apiClient.fetchDataPoolSettings(orgId, dataPoolId))
                .thenThrow(new DataPoolApiException("DATAPOOL_SETTINGS_ERROR", "API Error"));

        // Act & Assert
        assertThrows(DataPoolApiException.class, () ->
                dataPoolService.getDataPoolSettings(orgId, dataPoolId));

        verify(apiClient, times(1)).fetchDataPoolSettings(orgId, dataPoolId);
    }
}
