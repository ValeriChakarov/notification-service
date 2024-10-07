package com.example.notifications.messaging.producer;

import com.example.notifications.data.dtos.NotificationDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationKafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    @Value("${kafka.topic.notifications}")
    private String topic;

    public void sendNotification(NotificationDTO notificationDTO) {
        final ProducerRecord<String, NotificationDTO> record = createRecord(notificationDTO);

        //Async
        CompletableFuture<SendResult<String, NotificationDTO>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                handleSuccess(notificationDTO);
            } else {
                handleFailure(notificationDTO, ex);
            }
        });
    }

    private ProducerRecord<String, NotificationDTO> createRecord(NotificationDTO notificationDTO) {
        return new ProducerRecord<>(topic, notificationDTO.getId(), notificationDTO);
    }

    private void handleSuccess(NotificationDTO notificationDTO) {
        logger.info("Successfully sent data with ID [{}] to Kafka", notificationDTO.getId());
    }

    private void handleFailure(NotificationDTO notificationDTO, Throwable ex) {
        logger.error("Failed to send data with ID [{}] to Kafka. Error: {}", notificationDTO.getId(), ex.getMessage());
    }
}