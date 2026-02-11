package ai.armada.client.datapool.dto;

/**
 * Setting configuration
 */
public record SettingDto(
        String type,      // ALERT or LIMIT
        String period,    // DAY, WEEK, MONTH, ALL
        String unit,      // GB, TB, PB, PERCENT
        Float value
) {}
