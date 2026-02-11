package ai.armada.client.common.security;

import com.fasterxml.jackson.annotation.JsonProperty;

// Token Request Record
public record TokenRequest(
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("api_key_id") String apiKeyId
) {}
