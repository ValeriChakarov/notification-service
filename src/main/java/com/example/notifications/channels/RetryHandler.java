package com.example.notifications.channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryHandler {

    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);

    public static void executeWithRetry(Runnable operation, int maxRetries, long backoffMs) {
        int attempt = 0;
        boolean success = false;

        while (attempt < maxRetries && !success) {
            try {
                operation.run();
                success = true;
            } catch (Exception e) {
                attempt++;
                logger.error("Attempt [{}] failed: {}", attempt, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        if (!success) {
            logger.error("Exceeded maximum retry attempts [{}]", maxRetries);
        }
    }
}