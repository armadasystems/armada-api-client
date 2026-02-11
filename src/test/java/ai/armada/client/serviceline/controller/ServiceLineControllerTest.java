package ai.armada.client.serviceline.controller;

import ai.armada.client.serviceline.dto.*;
import ai.armada.client.serviceline.exception.ServiceLineApiException;
import ai.armada.client.serviceline.service.ServiceLineService;
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

@WebMvcTest(ServiceLineController.class)
class ServiceLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceLineService serviceLineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getServiceLines_WhenSuccessful_ShouldReturnServiceLineList() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        ServiceLineDto serviceLine1 = new ServiceLineDto(
                "sl-001",
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001", "KIT002")
        );
        ServiceLineDto serviceLine2 = new ServiceLineDto(
                "sl-002",
                "Service Line 2",
                "555-0002",
                "Active",
                LocalDate.of(2024, 2, 1),
                List.of("KIT003")
        );
        List<ServiceLineDto> serviceLines = List.of(serviceLine1, serviceLine2);

        when(serviceLineService.getServiceLines(orgId, dataPoolId)).thenReturn(serviceLines);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines", orgId, dataPoolId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("sl-001"))
                .andExpect(jsonPath("$[0].serviceLineName").value("Service Line 1"))
                .andExpect(jsonPath("$[0].serviceLineNumber").value("555-0001"))
                .andExpect(jsonPath("$[0].status").value("Active"))
                .andExpect(jsonPath("$[1].id").value("sl-002"))
                .andExpect(jsonPath("$[1].serviceLineName").value("Service Line 2"));
    }

    @Test
    void getServiceLines_WhenEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(serviceLineService.getServiceLines(orgId, dataPoolId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines", orgId, dataPoolId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getServiceLines_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(serviceLineService.getServiceLines(orgId, dataPoolId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_FETCH_ERROR", "Failed to fetch"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines", orgId, dataPoolId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("SERVICELINE_FETCH_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getServiceLineById_WhenSuccessful_ShouldReturnServiceLine() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        ServiceLineDto serviceLine = new ServiceLineDto(
                "sl-001",
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001", "KIT002")
        );

        when(serviceLineService.getServiceLineById(orgId, dataPoolId, serviceLineId)).thenReturn(serviceLine);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}",
                        orgId, dataPoolId, serviceLineId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("sl-001"))
                .andExpect(jsonPath("$.serviceLineName").value("Service Line 1"))
                .andExpect(jsonPath("$.serviceLineNumber").value("555-0001"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.kitNumbers.length()").value(2));
    }

    @Test
    void getServiceLineById_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        when(serviceLineService.getServiceLineById(orgId, dataPoolId, serviceLineId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_FETCH_ERROR", "Failed to fetch"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}",
                        orgId, dataPoolId, serviceLineId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("SERVICELINE_FETCH_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getServiceLineUsage_WhenSuccessful_ShouldReturnUsage() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        Integer billingCycles = 2;

        ServiceLineUsageDto usage = new ServiceLineUsageDto(
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001"),
                List.of()
        );

        when(serviceLineService.getServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles)).thenReturn(usage);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/data-usage",
                        orgId, dataPoolId, serviceLineId)
                        .param("billingCycles", billingCycles.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.serviceLineName").value("Service Line 1"))
                .andExpect(jsonPath("$.serviceLineNumber").value("555-0001"))
                .andExpect(jsonPath("$.billingCycles").isArray());
    }

    @Test
    void getServiceLineUsage_WhenDefaultBillingCycles_ShouldUseDefaultValue() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";

        ServiceLineUsageDto usage = new ServiceLineUsageDto(
                "Service Line 1",
                "555-0001",
                "Active",
                LocalDate.of(2024, 1, 1),
                List.of("KIT001"),
                List.of()
        );

        when(serviceLineService.getServiceLineUsage(orgId, dataPoolId, serviceLineId, 1)).thenReturn(usage);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/data-usage",
                        orgId, dataPoolId, serviceLineId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.serviceLineName").value("Service Line 1"))
                .andExpect(jsonPath("$.serviceLineNumber").value("555-0001"));
    }

    @Test
    void getServiceLineUsage_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        when(serviceLineService.getServiceLineUsage(orgId, dataPoolId, serviceLineId, 1))
                .thenThrow(new ServiceLineApiException("SERVICELINE_USAGE_ERROR", "Failed to fetch usage"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/data-usage",
                        orgId, dataPoolId, serviceLineId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("SERVICELINE_USAGE_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getAllServiceLinesUsage_WhenSuccessful_ShouldReturnUsage() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        Integer billingCycles = 2;

        ServiceLinesUsageDto usage = new ServiceLinesUsageDto(
                "dp-001",
                List.of()
        );

        when(serviceLineService.getAllServiceLinesUsage(orgId, dataPoolId, billingCycles)).thenReturn(usage);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/data-usage",
                        orgId, dataPoolId)
                        .param("billingCycles", billingCycles.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value("dp-001"))
                .andExpect(jsonPath("$.serviceLines").isArray());
    }

    @Test
    void getAllServiceLinesUsage_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(serviceLineService.getAllServiceLinesUsage(orgId, dataPoolId, 1))
                .thenThrow(new ServiceLineApiException("SERVICELINE_USAGE_ERROR", "Failed to fetch usage"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/data-usage",
                        orgId, dataPoolId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("SERVICELINE_USAGE_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getServiceLineSettings_WhenSuccessful_ShouldReturnSettings() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";

        ServiceLineSettingsDto settings = new ServiceLineSettingsDto(
                "sl-001",
                List.of(),
                List.of()
        );

        when(serviceLineService.getServiceLineSettings(orgId, dataPoolId, serviceLineId)).thenReturn(settings);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/settings",
                        orgId, dataPoolId, serviceLineId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.serviceLineId").value("sl-001"))
                .andExpect(jsonPath("$.settings").isArray())
                .andExpect(jsonPath("$.notifications").isArray());
    }

    @Test
    void getServiceLineSettings_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        String serviceLineId = "sl-001";
        when(serviceLineService.getServiceLineSettings(orgId, dataPoolId, serviceLineId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_SETTINGS_ERROR", "Failed to fetch settings"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/settings",
                        orgId, dataPoolId, serviceLineId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("SERVICELINE_SETTINGS_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getAllServiceLinesSettings_WhenSuccessful_ShouldReturnSettingsList() throws Exception {
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

        List<ServiceLineSettingsDto> settingsList = List.of(settings1, settings2);

        when(serviceLineService.getAllServiceLinesSettings(orgId, dataPoolId)).thenReturn(settingsList);

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/settings",
                        orgId, dataPoolId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].serviceLineId").value("sl-001"))
                .andExpect(jsonPath("$[1].serviceLineId").value("sl-002"));
    }

    @Test
    void getAllServiceLinesSettings_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        String orgId = "org-123";
        String dataPoolId = "dp-001";
        when(serviceLineService.getAllServiceLinesSettings(orgId, dataPoolId))
                .thenThrow(new ServiceLineApiException("SERVICELINE_SETTINGS_ERROR", "Failed to fetch settings"));

        // Act & Assert
        mockMvc.perform(get("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/settings",
                        orgId, dataPoolId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("SERVICELINE_SETTINGS_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }
}
