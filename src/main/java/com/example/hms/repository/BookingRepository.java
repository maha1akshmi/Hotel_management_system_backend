package com.example.hms.repository;

import com.example.hms.entity.Booking;
import com.example.hms.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByStatus(BookingStatus status);

    // Sum total revenue from confirmed + completed bookings
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status IN ('CONFIRMED', 'COMPLETED')")
    BigDecimal sumTotalRevenue();

    // Get 10 most recent bookings for dashboard widget
    List<Booking> findTop10ByOrderByBookedAtDesc();
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
