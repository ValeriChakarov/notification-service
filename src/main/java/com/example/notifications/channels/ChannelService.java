package com.example.notifications.channels;

import com.example.notifications.data.dtos.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {

    @Autowired
    private EmailNotificationChannel emailNotificationChannel;

    @Autowired
    private SMSNotificationChannel smsNotificationChannel;

    @Autowired
    private SlackNotificationChannel slackNotificationChannel;

    public void dispatchNotificationToChannel(NotificationDTO notificationDTO) {
        NotificationChannelType channelType = NotificationChannelType.valueOf(notificationDTO.getChannel().toUpperCase());
        NotificationChannel channel = routeChannel(channelType);
        channel.send(notificationDTO);
    }

    private NotificationChannel routeChannel(NotificationChannelType channelType) {
        return switch (channelType) {
            case EMAIL -> emailNotificationChannel;
            case SMS -> smsNotificationChannel;
            case SLACK -> slackNotificationChannel;
            default -> throw new IllegalArgumentException("Unsupported notification channel: " + channelType);
        };
    }
}
