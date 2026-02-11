package ai.armada.client.serviceline.service;

import ai.armada.client.serviceline.client.ServiceLineApiClient;
import ai.armada.client.serviceline.dto.*;
import ai.armada.client.serviceline.mapper.ServiceLineMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ServiceLineService {

    private final ServiceLineApiClient apiClient;
    private final ServiceLineMapper mapper;
    
    public ServiceLineService(ServiceLineApiClient apiClient, ServiceLineMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    public List<ServiceLineDto> getServiceLines(String orgId, String dataPoolId) {
        log.info("Retrieving service lines for data pool: {}", dataPoolId);
        
        List<ServiceLineDto> serviceLines = apiClient.fetchServiceLines(orgId, dataPoolId).stream()
                .map(mapper::toDto)
                .toList();
        
        log.info("Retrieved {} service lines", serviceLines.size());
        return serviceLines;
    }

    public ServiceLineDto getServiceLineById(String orgId, String dataPoolId, String serviceLineId) {
        log.info("Retrieving service line: {} for data pool: {}", serviceLineId, dataPoolId);
        
        ExternalServiceLineDto external = apiClient.fetchServiceLineById(orgId, dataPoolId, serviceLineId);
        return mapper.toDto(external);
    }

    public ServiceLineUsageDto getServiceLineUsage(String orgId, String dataPoolId, String serviceLineId, Integer billingCycles) {
        log.info("Retrieving usage for service line: {} with {} billing cycles",
                serviceLineId, billingCycles);

        return apiClient.fetchServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles);
    }

    public ServiceLinesUsageDto getAllServiceLinesUsage(String orgId, String dataPoolId, Integer billingCycles) {
        log.info("Retrieving usage for all service lines in data pool: {} with {} billing cycles", 
                dataPoolId, billingCycles);
        
        return apiClient.fetchAllServiceLinesUsage(orgId, dataPoolId, billingCycles);
    }

    public ServiceLineSettingsDto getServiceLineSettings(String orgId, String dataPoolId, String serviceLineId) {
        log.info("Retrieving settings for service line: {}", serviceLineId);
        
        return apiClient.fetchServiceLineSettings(orgId, dataPoolId, serviceLineId);
    }

    public List<ServiceLineSettingsDto> getAllServiceLinesSettings(String orgId, String dataPoolId) {
        log.info("Retrieving settings for all service lines in data pool: {}", dataPoolId);
        
        return apiClient.fetchAllServiceLinesSettings(orgId, dataPoolId);
    }
}