package com.example.hms.controller;

import com.example.hms.dto.*;
import com.example.hms.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin-only endpoints for managing the hotel system")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Returns total counts and revenue for users, hotels, bookings, and payments")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        AdminStatsResponse stats = adminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users (paged)", description = "Returns a paginated list of all registered users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<UserResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved"));
    }

    @PatchMapping("/users/{id}/ban")
    @Operation(summary = "Toggle ban/unban a user", description = "Toggles the active status of a user (ban or unban)")
    public ResponseEntity<ApiResponse<UserResponse>> toggleBanUser(@PathVariable Long id) {
        UserResponse user = adminService.toggleBanUser(id);
        String action = user.getIsActive() ? "unbanned" : "banned";
        return ResponseEntity.ok(ApiResponse.success(user, "User " + action + " successfully"));
    }

    @GetMapping("/hotels")
    @Operation(summary = "Get all hotels (paged)", description = "Returns a paginated list of all hotels with their approval status")
    public ResponseEntity<ApiResponse<PagedResponse<HotelResponse>>> getAllHotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<HotelResponse> hotels = adminService.getAllHotels(pageable);
        return ResponseEntity.ok(ApiResponse.success(hotels, "Hotels retrieved"));
    }

    @PatchMapping("/hotels/{id}/approve")
    @Operation(summary = "Approve or reject a hotel", description = "Updates a hotel's status to APPROVED or REJECTED")
    public ResponseEntity<ApiResponse<HotelResponse>> approveHotel(
            @PathVariable Long id,
            @RequestParam String status) {
        HotelResponse hotel = adminService.approveHotel(id, status);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel status updated to " + hotel.getStatus()));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get all bookings (paged)", description = "Returns a paginated list of all bookings across the system")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<BookingResponse> bookings = adminService.getAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved"));
    }
}
