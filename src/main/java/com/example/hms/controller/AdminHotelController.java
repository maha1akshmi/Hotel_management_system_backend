package com.example.hms.controller;

import com.example.hms.dto.*;
import com.example.hms.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels (Admin)", description = "Admin endpoints for managing hotels and rooms")
public class AdminHotelController {

    private final HotelService hotelService;

    @PostMapping
    @Operation(summary = "Create a new hotel", description = "Admin creates a new hotel listing (status defaults to PENDING)")
    public ResponseEntity<ApiResponse<HotelDetailResponse>> createHotel(
            @Valid @RequestBody HotelCreateRequest request) {
        HotelDetailResponse hotel = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(hotel, "Hotel created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a hotel", description = "Admin updates an existing hotel's details")
    public ResponseEntity<ApiResponse<HotelDetailResponse>> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelUpdateRequest request) {
        HotelDetailResponse hotel = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a hotel", description = "Admin deletes a hotel and all its rooms")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/rooms")
    @Operation(summary = "Add a room to a hotel", description = "Admin adds a new room type to an existing hotel")
    public ResponseEntity<ApiResponse<RoomResponse>> addRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomCreateRequest request) {
        RoomResponse room = hotelService.addRoom(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(room, "Room added successfully"));
    }
}
