package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushNotificationController {

    private final PushNotificationService pushService;

    public PushNotificationController(PushNotificationService pushService) {
        this.pushService = pushService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String title = request.getOrDefault("title", "📦 Nueva entrega asignada");
        String body = request.getOrDefault("body", "Tocá para ver los detalles en la app");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token faltante"));
        }

        pushService.sendPushNotification(token, title, body);
        return ResponseEntity.ok(Map.of("status", "Notificación enviada"));
    }
}
