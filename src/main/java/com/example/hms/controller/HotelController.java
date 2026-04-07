package com.example.hms.controller;

import com.example.hms.dto.*;
import com.example.hms.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels (Public)", description = "Public endpoints for searching and viewing hotels")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/search")
    @Operation(summary = "Search hotels", description = "Search approved hotels by city, check-in/out dates, and number of guests")
    public ResponseEntity<ApiResponse<PagedResponse<HotelSearchResponse>>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false) Integer guests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<HotelSearchResponse> results = hotelService.searchHotels(city, checkIn, checkOut, guests, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Hotels found"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel details", description = "Get full details of a hotel including its rooms")
    public ResponseEntity<ApiResponse<HotelDetailResponse>> getHotelById(@PathVariable Long id) {
        HotelDetailResponse hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel details retrieved"));
    }

    @GetMapping("/{id}/rooms")
    @Operation(summary = "Get hotel rooms", description = "List all available rooms for a hotel")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getHotelRooms(@PathVariable Long id) {
        List<RoomResponse> rooms = hotelService.getHotelRooms(id);
        return ResponseEntity.ok(ApiResponse.success(rooms, "Rooms retrieved"));
    }
}
