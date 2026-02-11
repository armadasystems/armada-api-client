package ai.armada.client.datapool.dto;

import java.time.LocalDate;

/**
 * Daily data usage
 */
public record DailyUsageDto(
        LocalDate date,
        Float dataUsageGB
) {}
