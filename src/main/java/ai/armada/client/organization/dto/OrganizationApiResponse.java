package ai.armada.client.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for the organization API response containing status, data, and metadata.
 * Handles the standard Armada API response format.
 */
public record OrganizationApiResponse(
        String status,
        ExternalOrganizationDto data,
        ResponseMetadata metadata
) {
    /**
     * Response metadata record.
     */
    public record ResponseMetadata(
            String timestamp,
            @JsonProperty("request_id") String requestId,
            @JsonProperty("api_version") String apiVersion
    ) {}

    /**
     * Check if the response indicates success.
     */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
