package com.example.hms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;

    // Hotel info
    private Long hotelId;
    private String hotelName;
    private String hotelCity;

    // Room info
    private Long roomId;
    private String roomType;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;
    private Integer roomsBooked;
    private BigDecimal totalPrice;
    private String status;

    // Payment info
    private String paymentStatus;
    private String paymentMethod;

    private LocalDateTime bookedAt;
}
