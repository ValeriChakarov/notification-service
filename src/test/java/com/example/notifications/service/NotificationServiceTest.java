package com.example.notifications.service;

import com.example.notifications.channels.ChannelService;
import com.example.notifications.data.dtos.NotificationDTO;
import com.example.notifications.data.entities.NotificationEntity;
import com.example.notifications.data.redis.RedisNotification;
import com.example.notifications.data.redis.RedisNotificationService;
import com.example.notifications.data.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private ChannelService channelService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RedisNotificationService redisNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    @Value("${redis.notification.ttl}")
    private long ttl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendNotification() {
        NotificationDTO notificationDTO = new NotificationDTO("1", "Test message", "recipient@test.com", "email");

        notificationService.sendNotification(notificationDTO);

        verify(channelService, times(1)).dispatchNotificationToChannel(notificationDTO);
    }

    @Test
    void testSaveNotificationToDB() {
        NotificationDTO notificationDTO = new NotificationDTO("2", "Database test message", "dbRecipient@test.com", "sms");

        notificationService.saveNotificationToDB(notificationDTO, "processed");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity savedEntity = captor.getValue();

        assertEquals("2", savedEntity.getNotificationId());
        assertEquals("processed", savedEntity.getStatus());
        assertEquals("Database test message", savedEntity.getMessage());
        assertNotNull(savedEntity.getCreatedAt());
    }

    @Test
    void testMarkNotificationInRedis() {
        NotificationDTO notificationDTO = new NotificationDTO("3", "Redis test message", "redisRecipient@test.com", "slack");
        int retryCount = 1;
        String failureReason = "Failed to send";

        notificationService.markNotificationInRedis(notificationDTO, "failed", retryCount, failureReason);

        ArgumentCaptor<RedisNotification> captor = ArgumentCaptor.forClass(RedisNotification.class);
        verify(redisNotificationService).saveNotification(eq("3"), captor.capture(), eq(ttl));
        RedisNotification savedNotification = captor.getValue();

        assertEquals("3", savedNotification.getId());
        assertEquals("failed", savedNotification.getStatus());
        assertEquals(retryCount, savedNotification.getRetryCount());
        assertEquals("Failed to send", savedNotification.getFailureReason());
        assertNotNull(savedNotification.getLastAttemptAt());
    }
}
