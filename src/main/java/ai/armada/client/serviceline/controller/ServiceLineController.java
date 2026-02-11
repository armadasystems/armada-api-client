package ai.armada.client.serviceline.controller;

import ai.armada.client.serviceline.dto.*;
import ai.armada.client.serviceline.service.ServiceLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines")
@Slf4j
public class ServiceLineController {

    private final ServiceLineService service;

    public ServiceLineController(ServiceLineService service) {
        this.service = service;
    }

    /**
     * Get all service lines for a data pool
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines
     */
    @GetMapping
    public ResponseEntity<List<ServiceLineDto>> getServiceLines(
            @PathVariable String orgId,
            @PathVariable String dataPoolId) {
        log.info("Received request to get service lines for data pool: {}", dataPoolId);
        List<ServiceLineDto> serviceLines = service.getServiceLines(orgId, dataPoolId);
        return ResponseEntity.ok(serviceLines);
    }

    /**
     * Get a specific service line by ID
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}
     */
    @GetMapping("/{serviceLineId}")
    public ResponseEntity<ServiceLineDto> getServiceLineById(
            @PathVariable String orgId,
            @PathVariable String dataPoolId,
            @PathVariable String serviceLineId) {
        log.info("Received request to get service line: {}", serviceLineId);
        ServiceLineDto serviceLine = service.getServiceLineById(orgId, dataPoolId, serviceLineId);
        return ResponseEntity.ok(serviceLine);
    }

    /**
     * Get data usage for a specific service line
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/data-usage?billingCycles=1
     */
    @GetMapping("/{serviceLineId}/data-usage")
    public ResponseEntity<ServiceLineUsageDto> getServiceLineUsage(
            @PathVariable String orgId,
            @PathVariable String dataPoolId,
            @PathVariable String serviceLineId,
            @RequestParam(defaultValue = "1") Integer billingCycles) {
        log.info("Received request to get usage for service line: {} with {} billing cycles", 
                serviceLineId, billingCycles);
        ServiceLineUsageDto usage = service.getServiceLineUsage(orgId, dataPoolId, serviceLineId, billingCycles);
        return ResponseEntity.ok(usage);
    }

    /**
     * Get data usage for all service lines in a data pool
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/data-usage?billingCycles=1
     */
    @GetMapping("/data-usage")
    public ResponseEntity<ServiceLinesUsageDto> getAllServiceLinesUsage(
            @PathVariable String orgId,
            @PathVariable String dataPoolId,
            @RequestParam(defaultValue = "1") Integer billingCycles) {
        log.info("Received request to get usage for all service lines with {} billing cycles", billingCycles);
        ServiceLinesUsageDto usage = service.getAllServiceLinesUsage(orgId, dataPoolId, billingCycles);
        return ResponseEntity.ok(usage);
    }

    /**
     * Get settings for a specific service line
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/{serviceLineId}/settings
     */
    @GetMapping("/{serviceLineId}/settings")
    public ResponseEntity<ServiceLineSettingsDto> getServiceLineSettings(
            @PathVariable String orgId,
            @PathVariable String dataPoolId,
            @PathVariable String serviceLineId) {
        log.info("Received request to get settings for service line: {}", serviceLineId);
        ServiceLineSettingsDto settings = service.getServiceLineSettings(orgId, dataPoolId, serviceLineId);
        return ResponseEntity.ok(settings);
    }

    /**
     * Get settings for all service lines in a data pool
     * GET /api/organizations/{orgId}/data-pools/{dataPoolId}/service-lines/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<List<ServiceLineSettingsDto>> getAllServiceLinesSettings(
            @PathVariable String orgId,
            @PathVariable String dataPoolId) {
        log.info("Received request to get settings for all service lines in data pool: {}", dataPoolId);
        List<ServiceLineSettingsDto> settings = service.getAllServiceLinesSettings(orgId, dataPoolId);
        return ResponseEntity.ok(settings);
    }
}