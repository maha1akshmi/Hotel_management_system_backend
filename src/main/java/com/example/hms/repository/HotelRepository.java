package com.example.hms.repository;

import com.example.hms.entity.Hotel;
import com.example.hms.enums.HotelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    long countByStatus(HotelStatus status);
}
