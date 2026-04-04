package com.example.hms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer capacity;

    @Column(name = "price_per_night", precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "total_rooms")
    private Integer totalRooms;

    @Column(name = "available_rooms")
    private Integer availableRooms;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
