package com.example.hms.repository;

import com.example.hms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByHotelId(Long hotelId);

    // Find all active rooms for a hotel
    List<Room> findByHotelIdAndIsActiveTrue(Long hotelId);

    // Find all rooms for a hotel (including inactive — for admin)
    List<Room> findByHotelId(Long hotelId);

    // Get minimum price per night for a hotel (active rooms only)
    @Query("SELECT MIN(r.pricePerNight) FROM Room r WHERE r.hotel.id = :hotelId AND r.isActive = true")
    BigDecimal findMinPriceByHotelId(@Param("hotelId") Long hotelId);

    // Get total available rooms for a hotel
    @Query("SELECT COALESCE(SUM(r.availableRooms), 0) FROM Room r WHERE r.hotel.id = :hotelId AND r.isActive = true")
    int sumAvailableRoomsByHotelId(@Param("hotelId") Long hotelId);

    // Count active room types for a hotel
    @Query("SELECT COUNT(DISTINCT r.roomType) FROM Room r WHERE r.hotel.id = :hotelId AND r.isActive = true")
    int countRoomTypesByHotelId(@Param("hotelId") Long hotelId);

    // Find rooms with enough capacity for guests
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.isActive = true AND r.availableRooms > 0 AND r.capacity >= :guests")
    List<Room> findAvailableRooms(@Param("hotelId") Long hotelId, @Param("guests") int guests);
}
