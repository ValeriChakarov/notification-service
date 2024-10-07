package com.example.notifications.channels;

import com.example.notifications.data.dtos.NotificationDTO;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SMSNotificationChannel implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(SMSNotificationChannel.class);

    @Value("${notification.retry.limit}")
    private int retryLimit;

    @Value("${notification.retry.backOffPeriod}")
    private int backOffPeriod;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Override
    public void send(NotificationDTO notification) {
        RetryHandler.executeWithRetry(() -> sendSMS(notification), retryLimit, backOffPeriod);
    }

    public void sendSMS(NotificationDTO notification) {
        int maxRetries = 3;
        int attempt = 0;
        boolean sent = false;

        while (attempt < maxRetries && !sent) {
            try {
                Twilio.init(accountSid, authToken);

                Message message = Message.creator(
                                new PhoneNumber(notification.getRecipient()),
                                new PhoneNumber(twilioPhoneNumber),
                                notification.getMessage())
                        .create();

                logger.info("SMS sent successfully to [{}] with SID [{}]", notification.getRecipient(), message.getSid());
                sent = true;
            } catch (Exception e) {
                attempt++;
                logger.error("Attempt [{}] - Failed to send SMS to [{}]: {}", attempt, notification.getRecipient(), e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!sent) {
            logger.error("Failed to send SMS to [{}] after [{}] attempts", notification.getRecipient(), maxRetries);
        }
    }
}
