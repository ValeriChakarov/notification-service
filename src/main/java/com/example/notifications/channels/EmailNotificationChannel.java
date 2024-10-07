package com.example.notifications.channels;

import com.example.notifications.data.dtos.NotificationDTO;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationChannel.class);

    private final MailjetClient client;

    @Value("${notification.retry.limit}")
    private int retryLimit;

    @Value("${notification.retry.backOffPeriod}")
    private int backOffPeriod;

    @Value("${mailjet.from.email}")
    private String fromEmail;

    public EmailNotificationChannel(@Value("${mailjet.api.key}") String apiKey,
                                    @Value("${mailjet.api.secret}") String apiSecret) {
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiSecret)
                .build();

        this.client = new MailjetClient(options);
    }

    @Override
    public void send(NotificationDTO notification) {
        RetryHandler.executeWithRetry(() -> sendToEmail(notification), retryLimit, backOffPeriod);
    }

    public void sendToEmail(NotificationDTO notification) {
        try {
            JSONObject message = new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject().put("Email", fromEmail).put("Name", "YourService"))
                    .put(Emailv31.Message.TO, new JSONArray().put(new JSONObject().put("Email", notification.getRecipient())))
                    .put(Emailv31.Message.SUBJECT, "Notification from our service")
                    .put(Emailv31.Message.TEXTPART, notification.getMessage());

            MailjetRequest request = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES, new JSONArray().put(message));

            MailjetResponse response = client.post(request);

            if (response.getStatus() == 200) {
                logger.info("Email sent successfully to [{}]: {}", notification.getRecipient(), notification.getMessage());
            } else {
                logger.error("Failed to send email. Status: {}, Body: {}", response.getStatus(), response.getData());
            }
        } catch (MailjetException e) {
            logger.error("Failed to send email to [{}]: {}", notification.getRecipient(), e.getMessage(), e);
        }
    }
}
