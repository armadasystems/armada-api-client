package ai.armada.client.datapool.controller;

import ai.armada.client.datapool.dto.*;
import ai.armada.client.datapool.service.DataPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/data-pools")
@Slf4j
public class DataPoolController {

    private final DataPoolService service;

    public DataPoolController(DataPoolService service) {
        this.service = service;
    }

    /**
     * Get all data pools for an organization
     * GET /api/organizations/{orgId}/data-pools
     */
    @GetMapping
    public ResponseEntity<List<DataPoolDto>> getDataPools(@PathVariable String orgId) {
        log.info("Received request to get data pools for organization: {}", orgId);
        List<DataPoolDto> dataPools = service.getDataPools(orgId);
        return ResponseEntity.ok(dataPools);
    }

    /**
     * Get a specific data pool by ID
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}
     */
    @GetMapping("/{dataPoolId}")
    public ResponseEntity<DataPoolDto> getDataPoolById(
            @PathVariable String orgId,
            @PathVariable String dataPoolId) {
        log.info("Received request to get data pool: {} for organization: {}", dataPoolId, orgId);
        DataPoolDto dataPool = service.getDataPoolById(orgId, dataPoolId);
        return ResponseEntity.ok(dataPool);
    }

    /**
     * Get data usage for a data pool
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/data-usage?billingCycles=1
     */
    @GetMapping("/{dataPoolId}/data-usage")
    public ResponseEntity<DataPoolDataUsageDto> getDataPoolUsage(
            @PathVariable String orgId,
            @PathVariable String dataPoolId,
            @RequestParam(defaultValue = "1") Integer billingCycles) {
        log.info("Received request to get data usage for data pool: {} with {} billing cycles", 
                dataPoolId, billingCycles);
        DataPoolDataUsageDto usage = service.getDataPoolUsage(orgId, dataPoolId, billingCycles);
        return ResponseEntity.ok(usage);
    }

    /**
     * Get settings for a data pool
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/settings
     */
    @GetMapping("/{dataPoolId}/settings")
    public ResponseEntity<DataPoolSettingsDto> getDataPoolSettings(
            @PathVariable String orgId,
            @PathVariable String dataPoolId) {
        log.info("Received request to get settings for data pool: {}", dataPoolId);
        DataPoolSettingsDto settings = service.getDataPoolSettings(orgId, dataPoolId);
        return ResponseEntity.ok(settings);
    }
}