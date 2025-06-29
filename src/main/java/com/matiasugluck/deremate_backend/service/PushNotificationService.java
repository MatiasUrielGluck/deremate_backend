package com.matiasugluck.deremate_backend.service;

public interface PushNotificationService {
    void sendPushNotification(String expoPushToken, String title, String body);
}
