package com.example.notifications.data.repository;

import com.example.notifications.data.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    NotificationEntity findByNotificationId(String notificationId);

    List<NotificationEntity> findAllByStatus(String status);

}