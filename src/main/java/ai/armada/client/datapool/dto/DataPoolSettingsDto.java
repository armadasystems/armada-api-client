package ai.armada.client.datapool.dto;

import java.util.List;

/**
 * Data pool settings
 */
public record DataPoolSettingsDto(
        List<SettingDto> settings,
        List<NotificationDto> notifications
) {}
