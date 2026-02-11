package ai.armada.client.serviceline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * External API response for Service Line
 */
public record ExternalServiceLineDto(
        String id,
        @JsonProperty("serviceLineName") String serviceLineName,
        @JsonProperty("serviceLineNumber") String serviceLineNumber,
        String status,
        @JsonProperty("activationDate") LocalDate activationDate,
        @JsonProperty("kitNumbers") List<String> kitNumbers
) {}