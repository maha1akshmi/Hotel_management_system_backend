package com.example.hms.repository;

import com.example.hms.entity.Hotel;
import com.example.hms.enums.HotelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    long countByStatus(HotelStatus status);

    // Search hotels by city (case-insensitive) that are APPROVED
    @Query("SELECT h FROM Hotel h WHERE h.status = 'APPROVED' " +
           "AND (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))")
    Page<Hotel> searchHotels(@Param("city") String city, Pageable pageable);

    // Find all approved hotels
    Page<Hotel> findByStatus(HotelStatus status, Pageable pageable);
}
