package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.NotificationMessage;
import com.matiasugluck.deremate_backend.entity.User;
import org.springframework.http.ResponseEntity;

public interface FirebaseNotificationService {
    ResponseEntity<GenericResponseDTO<String>> linkUser(String firebaseDeviceToken, User user);
    ResponseEntity<GenericResponseDTO<String>> unlinkUser(String firebaseDeviceToken, User user);
    String sendNotification(NotificationMessage notificationMessage);
}
