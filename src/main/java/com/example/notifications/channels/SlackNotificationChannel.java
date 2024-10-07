package com.example.notifications.channels;

import com.example.notifications.data.dtos.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class SlackNotificationChannel implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(SlackNotificationChannel.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notification.retry.limit}")
    private int retryLimit;

    @Value("${notification.retry.backOffPeriod}")
    private int backOffPeriod;

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @Override
    public void send(NotificationDTO notification) {
        RetryHandler.executeWithRetry(() -> sendToSlack(notification), retryLimit, backOffPeriod);
    }

    public void sendToSlack(NotificationDTO notification) {
        try {
            String payload = createSlackPayload(notification);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully sent Slack notification to channel [{}] with message: [{}]", notification.getRecipient(), notification.getMessage());
            } else {
                logger.error("Failed to send Slack notification. HTTP Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error occurred while sending Slack notification: {}", e.getMessage(), e);
        }
    }

    private String createSlackPayload(NotificationDTO notification) {
        return String.format("{\"text\": \"Message to %s: %s\"}", notification.getRecipient(), notification.getMessage());
    }
}
