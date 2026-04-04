package com.example.hms.repository;

import com.example.hms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Minimal repo for Room lookups needed by Booking module.
 * Module 2 (Jeyanth) may add more query methods.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
