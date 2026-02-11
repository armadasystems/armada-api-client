package ai.armada.client.organization.controller;

import ai.armada.client.organization.dto.OrganizationDto;
import ai.armada.client.organization.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orgs")
@Slf4j
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getOrganizations() {
        log.info("Received request to get organizations");
        List<OrganizationDto> organizations = service.getOrganizations();
        return ResponseEntity.ok(organizations);
    }
}