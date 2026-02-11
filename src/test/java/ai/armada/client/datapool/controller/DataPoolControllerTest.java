package ai.armada.client.datapool.controller;

import ai.armada.client.datapool.dto.*;
import ai.armada.client.datapool.exception.DataPoolApiException;
import ai.armada.client.datapool.service.DataPoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataPoolController.class)
class DataPoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataPoolService dataPoolService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDataPools_WhenSuccessful_ShouldReturnDataPoolList() throws Exception {
        // Arrange
        String orgId = "org-123";
        DataPoolDto dataPool1 = new DataPoolDto(
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
        DataPoolDto dataPool2 = new DataPoolDto(
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
        List<DataPoolDto> dataPools = List.of(dataPool1, dataPool2);

        when(dataPoolService.getDataPools(orgId)).thenReturn(dataPools);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools", orgId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("dp-001"))
                .andExpect(jsonPath("$[0].name").value("Data Pool 1"))
                .andExpect(jsonPath("$[0].country").value("USA"))
                .andExpect(jsonPath("$[0].planType").value("Premium"))
                .andExpect(jsonPath("$[0].status").value("Active"))
                .andExpect(jsonPath("$[1].id").value("dp-002"))
                .andExpect(jsonPath("$[1].name").value("Data Pool 2"));
    }

    @Test
    void getDataPools_WhenEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        String orgId = "org-123";
        when(dataPoolService.getDataPools(orgId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools", orgId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getDataPools_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        when(dataPoolService.getDataPools(orgId))
                .thenThrow(new DataPoolApiException("DATAPOOL_FETCH_ERROR", "Failed to fetch"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools", orgId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("DATAPOOL_FETCH_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getDataPoolById_WhenSuccessful_ShouldReturnDataPool() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        DataPoolDto dataPool = new DataPoolDto(
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

        when(dataPoolService.getDataPoolById(orgId, dataPoolId)).thenReturn(dataPool);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}", orgId, dataPoolId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("dp-001"))
                .andExpect(jsonPath("$.name").value("Data Pool 1"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.planType").value("Premium"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.dataAvailableGB").value(100.0))
                .andExpect(jsonPath("$.dataUsedGB").value(50.0))
                .andExpect(jsonPath("$.totalServiceLines").value(5));
    }

    @Test
    void getDataPoolById_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(dataPoolService.getDataPoolById(orgId, dataPoolId))
                .thenThrow(new DataPoolApiException("DATAPOOL_FETCH_ERROR", "Failed to fetch"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}", orgId, dataPoolId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("DATAPOOL_FETCH_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getDataPoolUsage_WhenSuccessful_ShouldReturnUsage() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 2;

        DataPoolDataUsageDto usage = new DataPoolDataUsageDto(
                List.of()
        );

        when(dataPoolService.getDataPoolUsage(orgId, dataPoolId, billingCycles)).thenReturn(usage);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/data-usage", orgId, dataPoolId)
                        .param("billingCycles", billingCycles.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.billingCycles").isArray());
    }

    @Test
    void getDataPoolUsage_WhenDefaultBillingCycles_ShouldUseDefaultValue() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        DataPoolDataUsageDto usage = new DataPoolDataUsageDto(
                List.of()
        );

        when(dataPoolService.getDataPoolUsage(orgId, dataPoolId, 1)).thenReturn(usage);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/data-usage", orgId, dataPoolId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.billingCycles").isArray());
    }

    @Test
    void getDataPoolUsage_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(dataPoolService.getDataPoolUsage(orgId, dataPoolId, 1))
                .thenThrow(new DataPoolApiException("DATAPOOL_USAGE_ERROR", "Failed to fetch usage"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/data-usage", orgId, dataPoolId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("DATAPOOL_USAGE_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getDataPoolSettings_WhenSuccessful_ShouldReturnSettings() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";

        DataPoolSettingsDto settings = new DataPoolSettingsDto(
                List.of(),
                List.of()
        );

        when(dataPoolService.getDataPoolSettings(orgId, dataPoolId)).thenReturn(settings);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/settings", orgId, dataPoolId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.settings").isArray())
                .andExpect(jsonPath("$.notifications").isArray());
    }

    @Test
    void getDataPoolSettings_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(dataPoolService.getDataPoolSettings(orgId, dataPoolId))
                .thenThrow(new DataPoolApiException("DATAPOOL_SETTINGS_ERROR", "Failed to fetch settings"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/settings", orgId, dataPoolId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("DATAPOOL_SETTINGS_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }
}
