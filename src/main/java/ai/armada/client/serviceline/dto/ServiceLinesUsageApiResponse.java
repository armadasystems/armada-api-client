package ai.armada.client.serviceline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for service lines usage API response containing status, data, and metadata.
 * Handles the standard Armada API response format for all service lines usage.
 */
public record ServiceLinesUsageApiResponse(
        String status,
        ServiceLinesUsageDto data,
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
