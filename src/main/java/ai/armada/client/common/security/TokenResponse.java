package ai.armada.client.common.security;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper for the API response containing status, data, and metadata.
 */
public record TokenResponse(
        String status,
        TokenData data,
        ResponseMetadata metadata
) {
    /**
     * Inner record containing the actual token data.
     */
    public record TokenData(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("organization_id") String organizationId
    ) {}

    /**
     * Inner record containing response metadata.
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

    /**
     * Get the access token from the nested data object.
     */
    public String getAccessToken() {
        return data != null ? data.accessToken() : null;
    }

    /**
     * Get the expires in value from the nested data object.
     */
    public long getExpiresIn() {
        return data != null ? data.expiresIn() : 0;
    }

    /**
     * Get the organization ID from the nested data object.
     */
    public String getOrganizationId() {
        return data != null ? data.organizationId() : null;
    }
}
