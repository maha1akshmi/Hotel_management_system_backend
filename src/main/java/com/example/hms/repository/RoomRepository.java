package com.example.hms.repository;

import com.example.hms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByHotelId(Long hotelId);
}
