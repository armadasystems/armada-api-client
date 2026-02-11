package ai.armada.client.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Internal DTO for Organization data
 */
public record OrganizationDto(
        String id,
        @JsonProperty("display_name") String displayName
) {}