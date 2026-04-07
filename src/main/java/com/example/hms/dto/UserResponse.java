package com.example.hms.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String oauthProvider;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
