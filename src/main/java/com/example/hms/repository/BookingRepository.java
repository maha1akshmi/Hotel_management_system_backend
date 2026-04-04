package com.example.hms.repository;

import com.example.hms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // All bookings for a specific user, ordered newest first
    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);

    // Find active (non-cancelled) bookings for a room that overlap with given dates
    List<Booking> findByRoomIdAndStatusNotAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomId,
            Booking.BookingStatus excludedStatus,
            LocalDate checkOut,
            LocalDate checkIn
    );
}
