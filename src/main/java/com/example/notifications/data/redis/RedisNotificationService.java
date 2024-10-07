package com.example.notifications.data.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RedisNotificationService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public void saveNotification(String id, RedisNotification notification, long expirationTime) {
        redisTemplate.opsForValue().set(id, notification, expirationTime, TimeUnit.SECONDS);
        logger.info("Saving notification [{}] in Redis with expiration time: {} seconds", notification.getId(), expirationTime);

    }

    public RedisNotification getNotification(String id) {
        RedisNotification notification = (RedisNotification) redisTemplate.opsForValue().get(id);
        if (notification == null) {
            logger.warn("Notification with id [{}] not found in Redis", id);
        }
        return notification;
    }

    public void deleteNotification(String id) {
        redisTemplate.delete(id);
        logger.info("Deleting notification with id [{}] has been deleted", id);

    }

    public boolean exists(String id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(id));
    }

    public int getRetryCount(String id) {
        RedisNotification notification = getNotification(id);
        return notification != null ? notification.getRetryCount() : 0;
    }
}