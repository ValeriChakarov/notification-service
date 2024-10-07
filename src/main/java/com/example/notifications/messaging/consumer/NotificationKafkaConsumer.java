package com.example.notifications.messaging.consumer;

import com.example.notifications.data.dtos.NotificationDTO;
import com.example.notifications.data.redis.RedisNotificationService;
import com.example.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationKafkaConsumer {

    @Value("${notification.retry.limit}")
    private int retryLimit;

    private static final Logger logger = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    @Autowired
    private RedisNotificationService redisNotificationService;

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.notifications}", groupId = "notification-group")
    public void consumeNotification(NotificationDTO notificationDTO) {
        String notificationId = notificationDTO.getId();

        if (redisNotificationService.exists(notificationId)) {
            logger.info("Notification with ID [{}] has already been processed!", notificationId);
            return;
        }

        // Fetch the current retry count from Redis (or default to 0)
        int retryCount = redisNotificationService.getRetryCount(notificationId);

        if (retryCount >= retryLimit) {
            logger.error("Notification with ID [{}] exceeded retry limit [{}]. Marking as permanently failed.", notificationId, retryLimit);
            notificationService.saveNotificationToDB(notificationDTO, "permanently_failed", "Retry limit exceeded", retryCount);
            return;
        }

        try {
            // Process the notification
            logger.info("Processing notification: {}", notificationDTO);
            notificationService.sendNotification(notificationDTO);

            // Save the notification in the DB and Redis with a "processed" status
            notificationService.saveNotificationToDB(notificationDTO, "processed");
            notificationService.markNotificationInRedis(notificationDTO, "processed", 0, null);  // No failure reason on success

        } catch (Exception e) {
            logger.error("Failed to process notification with ID [{}]: {}", notificationId, e.getMessage(), e);
            handleRetry(notificationDTO, retryCount, e.getMessage());
        }
    }

    private void handleRetry(NotificationDTO notificationDTO, int retryCount, String failureReason) {
        retryCount++;
        // Save the failed notification in the database with updated retry count and failure reason
        notificationService.saveNotificationToDB(notificationDTO, "failed", failureReason, retryCount);
        // Mark the notification in Redis with updated retry count and failure reason
        notificationService.markNotificationInRedis(notificationDTO, "failed", retryCount, failureReason);
    }
}
