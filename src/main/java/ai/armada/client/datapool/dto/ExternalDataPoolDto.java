package ai.armada.client.datapool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * External API response for Data Pool
 */
public record ExternalDataPoolDto(
        String id,
        String name,
        String country,
        @JsonProperty("planType") String planType,
        String status,
        @JsonProperty("startDate") LocalDate startDate,
        @JsonProperty("endDate") LocalDate endDate,
        @JsonProperty("dataAvailableGB") Float dataAvailableGB,
        @JsonProperty("dataUsedGB") Float dataUsedGB,
        @JsonProperty("totalServiceLines") Integer totalServiceLines
) {}