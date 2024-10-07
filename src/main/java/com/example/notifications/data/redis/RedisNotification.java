package com.example.notifications.data.redis;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisNotification {
    private String id;

    private String recipient;

    private String channel;

    private String status;

    private long expiration;

    private int retryCount;

    private String failureReason;

    private Instant lastAttemptAt;
}
