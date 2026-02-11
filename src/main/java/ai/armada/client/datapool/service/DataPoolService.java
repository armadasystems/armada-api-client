package ai.armada.client.datapool.service;

import ai.armada.client.datapool.client.DataPoolApiClient;
import ai.armada.client.datapool.dto.*;
import ai.armada.client.datapool.mapper.DataPoolMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DataPoolService {

    private final DataPoolApiClient apiClient;
    private final DataPoolMapper mapper;
    
    public DataPoolService(DataPoolApiClient apiClient, DataPoolMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    public List<DataPoolDto> getDataPools(String orgId) {
        log.info("Retrieving data pools for organization: {}", orgId);
        
        List<DataPoolDto> dataPools = apiClient.fetchDataPools(orgId).stream()
                .map(mapper::toDto)
                .toList();
        
        log.info("Retrieved {} data pools", dataPools.size());
        return dataPools;
    }

    public DataPoolDto getDataPoolById(String orgId, String dataPoolId) {
        log.info("Retrieving data pool: {} for organization: {}", dataPoolId, orgId);
        
        ExternalDataPoolDto external = apiClient.fetchDataPoolById(orgId, dataPoolId);
        return mapper.toDto(external);
    }

    public DataPoolDataUsageDto getDataPoolUsage(String orgId, String dataPoolId, Integer billingCycles) {
        log.info("Retrieving data usage for data pool: {} with {} billing cycles", 
                dataPoolId, billingCycles);
        
        return apiClient.fetchDataPoolUsage(orgId, dataPoolId, billingCycles);
    }

    public DataPoolSettingsDto getDataPoolSettings(String orgId, String dataPoolId) {
        log.info("Retrieving settings for data pool: {}", dataPoolId);
        
        return apiClient.fetchDataPoolSettings(orgId, dataPoolId);
    }
}