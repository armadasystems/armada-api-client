package ai.armada.client.datapool.dto;

import java.time.LocalDate;

/**
 * Internal DTO for Data Pool
 */
public record DataPoolDto(
        String id,
        String name,
        String country,
        String planType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Float dataAvailableGB,
        Float dataUsedGB,
        Integer totalServiceLines
) {}
