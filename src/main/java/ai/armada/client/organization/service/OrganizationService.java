package ai.armada.client.organization.service;

import ai.armada.client.organization.client.OrganizationApiClient;
import ai.armada.client.organization.dto.OrganizationDto;
import ai.armada.client.organization.mapper.OrganizationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrganizationService {

    private final OrganizationApiClient apiClient;
    private final OrganizationMapper mapper;
    
    public OrganizationService(OrganizationApiClient apiClient, OrganizationMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    public List<OrganizationDto> getOrganizations() {
        log.info("Retrieving organizations");
        
        List<OrganizationDto> organizations = apiClient.fetchOrganizations().stream()
                .map(mapper::toDto)
                .toList();
        
        log.info("Retrieved {} organizations", organizations.size());
        return organizations;
    }
}