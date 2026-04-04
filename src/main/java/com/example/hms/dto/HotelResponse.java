package com.example.hms.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String status;

    // Owner info
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private long roomCount;
    private LocalDateTime createdAt;
}
