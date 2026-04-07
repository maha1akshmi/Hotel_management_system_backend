package com.example.hms.service;

import com.example.hms.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {

    // ── Public endpoints ──
    PagedResponse<HotelSearchResponse> searchHotels(String city, String checkIn, String checkOut, Integer guests, Pageable pageable);

    HotelDetailResponse getHotelById(Long id);

    List<RoomResponse> getHotelRooms(Long hotelId);

    // ── Admin endpoints ──
    HotelDetailResponse createHotel(HotelCreateRequest request);

    HotelDetailResponse updateHotel(Long id, HotelUpdateRequest request);

    void deleteHotel(Long id);

    RoomResponse addRoom(Long hotelId, RoomCreateRequest request);
}
