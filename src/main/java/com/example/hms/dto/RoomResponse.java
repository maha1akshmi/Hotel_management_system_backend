package com.example.hms.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;
    private Long hotelId;
    private String roomType;
    private String description;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private Integer totalRooms;
    private Integer availableRooms;
    private Boolean isActive;
}
