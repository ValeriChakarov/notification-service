package com.example.notifications.service;

import com.example.notifications.channels.ChannelService;
import com.example.notifications.data.dtos.NotificationDTO;
import com.example.notifications.data.entities.NotificationEntity;
import com.example.notifications.data.redis.RedisNotification;
import com.example.notifications.data.redis.RedisNotificationService;
import com.example.notifications.data.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Autowired
    ChannelService channelService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RedisNotificationService redisNotificationService;


    @Value("${redis.notification.ttl}")
    private long ttl;

    public void sendNotification(NotificationDTO notificationDTO) {
        try {
            channelService.dispatchNotificationToChannel(notificationDTO);
            logger.info("Notification with ID [{}] was successfully dispatched to channel [{}] !", notificationDTO.getId(), notificationDTO.getChannel());
        } catch (Exception e) {
            logger.error("Failed to send notification with ID [{}] to the respective channel!: {}", notificationDTO.getId(), e.getMessage(), e);
        }
    }

    public void saveNotificationToDB(NotificationDTO notificationDTO, String status) {
        saveNotificationToDB(notificationDTO, status, null, 0);
    }

    public void saveNotificationToDB(NotificationDTO notificationDTO, String status, String failureReason, int retryCount) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setNotificationId(notificationDTO.getId());
        notificationEntity.setMessage(notificationDTO.getMessage());
        notificationEntity.setRecipient(notificationDTO.getRecipient());
        notificationEntity.setChannel(notificationDTO.getChannel());
        notificationEntity.setStatus(status);
        notificationEntity.setFailureReason(failureReason);
        notificationEntity.setRetryCount(retryCount);
        notificationEntity.setCreatedAt(Instant.now());
        notificationEntity.setLastAttemptAt(Instant.now());
        notificationRepository.save(notificationEntity);
        logger.info("Notification saved to DB: {} with status: {} and retry count: {}", notificationEntity.getNotificationId(), status, retryCount);
    }

    // Overloaded method to mark the notification in Redis with a failure reason
    public void markNotificationInRedis(NotificationDTO notificationDTO, String status, int retryCount, String failureReason) {
        String notificationId = notificationDTO.getId();
        RedisNotification redisNotification = new RedisNotification();
        redisNotification.setId(notificationId);
        redisNotification.setRecipient(notificationDTO.getRecipient());
        redisNotification.setChannel(notificationDTO.getChannel());
        redisNotification.setStatus(status);
        redisNotification.setExpiration(ttl);
        redisNotification.setRetryCount(retryCount);
        redisNotification.setFailureReason(status.equals("failed") ? failureReason : null);
        redisNotification.setLastAttemptAt(Instant.now());
        redisNotificationService.saveNotification(notificationId, redisNotification, ttl);
        logger.info("Notification with ID [{}] marked as [{}] in Redis with retry count [{}]. Failure Reason: {}.",
                notificationId, status, retryCount, failureReason);
    }
}