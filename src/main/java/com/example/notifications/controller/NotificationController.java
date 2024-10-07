package com.example.notifications.controller;


import com.example.notifications.data.dtos.NotificationDTO;
import com.example.notifications.messaging.producer.NotificationKafkaProducer;
import com.example.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationKafkaProducer notificationKafkaProducer;

    @Autowired
    private NotificationService notificationService;

    // Endpoint to send a notification manually via Kafka
    @PostMapping("/notification")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        logger.info("Received request to send notification: {}", notificationDTO);

        try {
            notificationKafkaProducer.sendNotification(notificationDTO);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception ex) {
            logger.error("Failed to send notification: {}", ex.getMessage());
            return ResponseEntity.status(500).body("Failed to send notification");
        }
    }

    //Endpoint to simulate sending notifications directly via channel (bypassing Kafka)
    @PostMapping("/directChannel")
    public ResponseEntity<String> sendNotificationDirectly(@Valid @RequestBody NotificationDTO notificationDTO) {
        logger.info("Received request to send notification directly: {}", notificationDTO);

        try {
            notificationService.sendNotification(notificationDTO);
            return ResponseEntity.ok("Notification sent successfully via direct channel");
        } catch (Exception ex) {
            logger.error("Failed to send notification directly: {}", ex.getMessage());
            return ResponseEntity.status(500).body("Failed to send notification directly");
        }
    }
}