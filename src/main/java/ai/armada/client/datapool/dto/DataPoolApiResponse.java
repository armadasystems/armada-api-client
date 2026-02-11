package ai.armada.client.datapool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wrapper for the data pool API response containing status, data, and metadata.
 * Handles the standard Armada API response format.
 */
public record DataPoolApiResponse(
        String status,
        List<ExternalDataPoolDto> data,
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
