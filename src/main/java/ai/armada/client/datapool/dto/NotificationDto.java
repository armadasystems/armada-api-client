package ai.armada.client.datapool.dto;

/**
 * Notification configuration
 */
public record NotificationDto(
        String type,      // EMAIL or GROUP
        String value
) {}
