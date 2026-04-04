package com.example.hms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private Long roomId;
    private String roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;
    private Integer roomsBooked;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime bookedAt;
}
