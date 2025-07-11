package com.matiasugluck.deremate_backend.dto.user;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private Boolean isEmailVerified;
    private int deliveriesCompleted;
    private Timestamp createdTime;
}
