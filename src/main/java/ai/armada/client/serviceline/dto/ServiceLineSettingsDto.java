package ai.armada.client.serviceline.dto;

import ai.armada.client.datapool.dto.NotificationDto;
import ai.armada.client.datapool.dto.SettingDto;

import java.util.List;

/**
 * Service line settings
 */
public record ServiceLineSettingsDto(
        String serviceLineId,
        List<SettingDto> settings,
        List<NotificationDto> notifications
) {}
