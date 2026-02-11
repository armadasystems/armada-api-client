package ai.armada.client.organization.service;

import ai.armada.client.organization.client.OrganizationApiClient;
import ai.armada.client.organization.dto.ExternalOrganizationDto;
import ai.armada.client.organization.dto.OrganizationDto;
import ai.armada.client.organization.exception.OrganizationApiException;
import ai.armada.client.organization.mapper.OrganizationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationApiClient apiClient;

    @Mock
    private OrganizationMapper mapper;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(apiClient, mapper);
    }

    @Test
    void getOrganizations_WhenSuccessful_ShouldReturnMappedOrganizations() {
        // Arrange
        ExternalOrganizationDto externalOrg1 = new ExternalOrganizationDto();
        externalOrg1.setId("org-123");
        externalOrg1.setName("Organization 1");
        externalOrg1.setDisplayName("Org 1");

        ExternalOrganizationDto externalOrg2 = new ExternalOrganizationDto();
        externalOrg2.setId("org-456");
        externalOrg2.setName("Organization 2");
        externalOrg2.setDisplayName("Org 2");

        List<ExternalOrganizationDto> externalOrgs = List.of(externalOrg1, externalOrg2);

        OrganizationDto mappedOrg1 = new OrganizationDto("org-123", "Org 1");
        OrganizationDto mappedOrg2 = new OrganizationDto("org-456", "Org 2");

        when(apiClient.fetchOrganizations()).thenReturn(externalOrgs);
        when(mapper.toDto(externalOrg1)).thenReturn(mappedOrg1);
        when(mapper.toDto(externalOrg2)).thenReturn(mappedOrg2);

        // Act
        List<OrganizationDto> result = organizationService.getOrganizations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("org-123", result.get(0).id());
        assertEquals("Org 1", result.get(0).displayName());
        assertEquals("org-456", result.get(1).id());
        assertEquals("Org 2", result.get(1).displayName());

        verify(apiClient, times(1)).fetchOrganizations();
        verify(mapper, times(2)).toDto(any(ExternalOrganizationDto.class));
    }

    @Test
    void getOrganizations_WhenEmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(apiClient.fetchOrganizations()).thenReturn(new ArrayList<>());

        // Act
        List<OrganizationDto> result = organizationService.getOrganizations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(apiClient, times(1)).fetchOrganizations();
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getOrganizations_WhenApiClientThrowsException_ShouldPropagateException() {
        // Arrange
        when(apiClient.fetchOrganizations())
                .thenThrow(new OrganizationApiException("API Error"));

        // Act & Assert
        assertThrows(OrganizationApiException.class, () -> 
                organizationService.getOrganizations());
        
        verify(apiClient, times(1)).fetchOrganizations();
        verify(mapper, never()).toDto(any());
    }

    @Test
    void getOrganizations_WhenMapperThrowsException_ShouldPropagateException() {
        // Arrange
        ExternalOrganizationDto externalOrg = new ExternalOrganizationDto();
        externalOrg.setId("org-123");
        
        when(apiClient.fetchOrganizations()).thenReturn(List.of(externalOrg));
        when(mapper.toDto(externalOrg)).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                organizationService.getOrganizations());
    }
}