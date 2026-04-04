package com.example.hms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelSearchResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String status;

    // Aggregated room info for search results
    private BigDecimal minPricePerNight;
    private int totalRoomTypes;
    private int totalAvailableRooms;

    private String ownerName;
    private LocalDateTime createdAt;
}
