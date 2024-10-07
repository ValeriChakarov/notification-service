package com.example.notifications.channels;

import com.example.notifications.data.dtos.NotificationDTO;

public interface NotificationChannel {

    void send(NotificationDTO notification);

}
