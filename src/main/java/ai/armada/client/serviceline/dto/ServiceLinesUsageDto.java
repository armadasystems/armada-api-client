package ai.armada.client.serviceline.dto;

import java.util.List;

/**
 * All service lines usage response
 */
public record ServiceLinesUsageDto(
        String id,
        List<ServiceLineListUsageDto> serviceLines
) {}
