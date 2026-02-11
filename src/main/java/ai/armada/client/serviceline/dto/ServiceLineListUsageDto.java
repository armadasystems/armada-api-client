package ai.armada.client.serviceline.dto;

import ai.armada.client.datapool.dto.BillingCycleUsageDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service line usage response for use in lists.
 * This DTO includes service line identification fields and is used when
 * fetching usage for multiple service lines where each item needs to be identified.
 */
public record ServiceLineListUsageDto(
        String serviceLineId,
        String serviceLineName,
        String serviceLineNumber,
        String status,
        LocalDate activationDate,
        List<String> kitNumbers,
        List<BillingCycleUsageDto> billingCycles
) {}
