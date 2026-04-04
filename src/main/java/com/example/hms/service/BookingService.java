package com.example.hms.service;

import com.example.hms.dto.BookingResponse;
import com.example.hms.dto.CreateBookingRequest;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(String userEmail, CreateBookingRequest request);
    List<BookingResponse> getMyBookings(String userEmail);
    BookingResponse getBookingById(String userEmail, Long bookingId);
    BookingResponse cancelBooking(String userEmail, Long bookingId);
}
