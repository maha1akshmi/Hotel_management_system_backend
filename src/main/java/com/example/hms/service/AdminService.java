package com.example.hms.service;

import com.example.hms.dto.*;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    AdminStatsResponse getStats();

    PagedResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse toggleBanUser(Long userId);

    PagedResponse<HotelResponse> getAllHotels(Pageable pageable);

    HotelResponse approveHotel(Long hotelId, String status);

    PagedResponse<BookingResponse> getAllBookings(Pageable pageable);
}
