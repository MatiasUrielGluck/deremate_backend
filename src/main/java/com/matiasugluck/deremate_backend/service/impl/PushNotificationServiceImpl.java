package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.service.PushNotificationService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void sendPushNotification(String expoPushToken, String title, String body) {
        String payload = """
        {
          "to": "%s",
          "title": "%s",
          "body": "%s",
          "data": { "screen": "Home" }
        }
        """.formatted(expoPushToken, title, body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://exp.host/--/api/v2/push/send",
                    request,
                    String.class
            );

            System.out.println("Expo push response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error al enviar notificación push: " + e.getMessage());
        }
    }
}
