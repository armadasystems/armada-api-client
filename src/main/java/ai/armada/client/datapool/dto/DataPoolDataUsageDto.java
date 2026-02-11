package ai.armada.client.datapool.dto;

import java.util.List;

/**
 * Data pool data usage response
 */
public record DataPoolDataUsageDto(
        List<BillingCycleUsageDto> billingCycles
) {}
