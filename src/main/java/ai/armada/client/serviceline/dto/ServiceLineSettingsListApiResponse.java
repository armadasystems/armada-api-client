package ai.armada.client.serviceline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wrapper for service line settings list API response containing status, data, and metadata.
 * Handles the standard Armada API response format for list of service line settings.
 */
public record ServiceLineSettingsListApiResponse(
        String status,
        List<ServiceLineSettingsDto> data,
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
