package ai.armada.client.organization.controller;

import ai.armada.client.organization.dto.OrganizationDto;
import ai.armada.client.organization.exception.OrganizationApiException;
import ai.armada.client.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getOrganizations_WhenSuccessful_ShouldReturnOrganizationList() throws Exception {
        // Arrange
        OrganizationDto org1 = new OrganizationDto("org-123", "Org 1");
        OrganizationDto org2 = new OrganizationDto("org-456", "Org 2");
        List<OrganizationDto> organizations = List.of(org1, org2);

        when(organizationService.getOrganizations()).thenReturn(organizations);

        // Act & Assert
        mockMvc.perform(get("/api/orgs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("org-123"))
                .andExpect(jsonPath("$[0].display_name").value("Org 1"))
                .andExpect(jsonPath("$[1].id").value("org-456"))
                .andExpect(jsonPath("$[1].display_name").value("Org 2"));
    }

    @Test
    void getOrganizations_WhenEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(organizationService.getOrganizations()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/orgs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOrganizations_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        when(organizationService.getOrganizations())
                .thenThrow(new OrganizationApiException("ORG_FETCH_ERROR", "Failed to fetch"));

        // Act & Assert
        mockMvc.perform(get("/api/orgs"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("ORG_FETCH_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }
}