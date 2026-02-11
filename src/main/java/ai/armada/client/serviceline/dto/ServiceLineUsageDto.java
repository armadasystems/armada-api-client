package ai.armada.client.serviceline.dto;

import ai.armada.client.datapool.dto.BillingCycleUsageDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service line usage response for a single service line.
 * This DTO is used when fetching usage for a specific service line by ID,
 * where the service line identifier is already known from the request context.
 */
public record ServiceLineUsageDto(
        String serviceLineName,
        String serviceLineNumber,
        String status,
        LocalDate activationDate,
        List<String> kitNumbers,
        List<BillingCycleUsageDto> billingCycles
) {}
