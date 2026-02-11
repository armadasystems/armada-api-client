package ai.armada.client.serviceline.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Internal DTO for Service Line
 */
public record ServiceLineDto(
        String id,
        String serviceLineName,
        String serviceLineNumber,
        String status,
        LocalDate activationDate,
        List<String> kitNumbers
) {}