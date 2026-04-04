package com.example.hms.controller;

import com.example.hms.dto.ApiResponse;
import com.example.hms.dto.BookingResponse;
import com.example.hms.dto.CreateBookingRequest;
import com.example.hms.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Create, view, and cancel bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a new booking", description = "Books a room at a hotel for the authenticated user")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            Authentication authentication,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        String email = authentication.getName();
        BookingResponse booking = bookingService.createBooking(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get my bookings", description = "Returns all bookings for the authenticated user")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(Authentication authentication) {
        String email = authentication.getName();
        List<BookingResponse> bookings = bookingService.getMyBookings(email);
        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details", description = "Returns a specific booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        BookingResponse booking = bookingService.getBookingById(email, id);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking retrieved successfully"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking", description = "Cancels a confirmed booking before check-in")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        BookingResponse booking = bookingService.cancelBooking(email, id);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled successfully"));
    }
}
