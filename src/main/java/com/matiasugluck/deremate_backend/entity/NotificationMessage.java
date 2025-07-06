package com.matiasugluck.deremate_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificationMessage {
    private String recipientToken;
    private String title;
    private String body;
    private String image;
    @Builder.Default
    private Map<String, String> data = new HashMap<>();

}
