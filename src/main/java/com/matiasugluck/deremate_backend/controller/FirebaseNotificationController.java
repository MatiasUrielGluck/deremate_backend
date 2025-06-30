package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.notification.NotificationLinkRequest;
import com.matiasugluck.deremate_backend.entity.NotificationMessage;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.FirebaseNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class FirebaseNotificationController {

    private final FirebaseNotificationService firebaseNotificationService;
    private final AuthService authService;

    public FirebaseNotificationController(FirebaseNotificationService firebaseNotificationService, AuthService authService) {
        this.firebaseNotificationService = firebaseNotificationService;
        this.authService = authService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationMessage notificationMessage) {
        String response = firebaseNotificationService.sendNotification(notificationMessage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/link-device")
    public  ResponseEntity<?> linkDevice(@RequestBody NotificationLinkRequest notificationLinkRequest) {
        User user = authService.getAuthenticatedUser();
        return firebaseNotificationService.linkUser(notificationLinkRequest.getFirebaseDeviceId(),user);
    }


}
