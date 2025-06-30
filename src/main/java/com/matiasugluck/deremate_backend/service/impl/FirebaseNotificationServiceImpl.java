package com.matiasugluck.deremate_backend.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.Device;
import com.matiasugluck.deremate_backend.entity.NotificationMessage;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.repository.DeviceRepository;
import com.matiasugluck.deremate_backend.service.FirebaseNotificationService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {
    final DeviceRepository deviceRepository;
    final FirebaseMessaging firebaseMessaging;

    public FirebaseNotificationServiceImpl(DeviceRepository deviceRepository, FirebaseMessaging firebaseMessaging) {
        this.deviceRepository = deviceRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public ResponseEntity<GenericResponseDTO<String>> linkUser(String firebaseDeviceToken, User user) {
        Device newDevice = Device.builder().user(user).deviceId(firebaseDeviceToken).build();
        deviceRepository.save(newDevice);
        GenericResponseDTO<String> response = new GenericResponseDTO<>();
        response.setData("Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public String sendNotification(NotificationMessage notificationMessage) {
        Notification notification = Notification.builder().setTitle(notificationMessage.getTitle()).setBody(notificationMessage.getBody()).setImage(notificationMessage.getImage()).build();
        Message message = Message.builder().setNotification(notification).setToken(notificationMessage.getRecipientToken()).putAllData(notificationMessage.getData()).build();

        try {
            firebaseMessaging.send(message);
            return "Success";
        }
        catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Failure";
        }
    }
}
