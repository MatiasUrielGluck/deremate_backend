package com.matiasugluck.deremate_backend.dto.notification;

import lombok.Data;

@Data
public class NotificationUnlinkRequest {
    private String firebaseDeviceId;
}
