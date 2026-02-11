package ai.armada.client.datapool.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Data usage for a billing cycle
 */
public record BillingCycleUsageDto(
        LocalDate startDate,
        LocalDate endDate,
        Float totalBillingCycleUsageGB,
        List<DailyUsageDto> dailyDataUsage
) {}
