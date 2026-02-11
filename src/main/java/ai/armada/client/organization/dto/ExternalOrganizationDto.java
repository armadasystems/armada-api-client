package ai.armada.client.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * External API response DTO for Organization
 * Maps to ApiKeyOrganizationResponseBody from swagger
 */
@Data
public class ExternalOrganizationDto {

    @JsonProperty("organization_id")
    private String id;

    @JsonProperty("organization_name")
    private String name;

    @JsonProperty("display_name")
    private String displayName;
}