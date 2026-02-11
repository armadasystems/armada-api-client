package ai.armada.client.organization.mapper;

import ai.armada.client.organization.dto.ExternalOrganizationDto;
import ai.armada.client.organization.dto.OrganizationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrganizationMapper {

    public OrganizationDto toDto(ExternalOrganizationDto external) {
        log.debug("Mapping external organization: {} to internal DTO", external.getId());
        
        return new OrganizationDto(
                external.getId(),
                external.getDisplayName()
        );
    }
}