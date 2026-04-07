package com.example.hms.service;

import com.example.hms.dto.*;
import com.example.hms.entity.Hotel;
import com.example.hms.entity.Room;
import com.example.hms.entity.User;
import com.example.hms.enums.HotelStatus;
import com.example.hms.exception.ResourceNotFoundException;
import com.example.hms.repository.HotelRepository;
import com.example.hms.repository.RoomRepository;
import com.example.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // ──────────────── Public Endpoints ────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HotelSearchResponse> searchHotels(String city, String checkIn, String checkOut, Integer guests, Pageable pageable) {
        // Search only APPROVED hotels matching city filter
        Page<Hotel> page = hotelRepository.searchHotels(city, pageable);

        List<HotelSearchResponse> content = page.getContent().stream()
                .map(hotel -> {
                    BigDecimal minPrice = roomRepository.findMinPriceByHotelId(hotel.getId());
                    int totalTypes = roomRepository.countRoomTypesByHotelId(hotel.getId());
                    int availableRooms = roomRepository.sumAvailableRoomsByHotelId(hotel.getId());

                    return HotelSearchResponse.builder()
                            .id(hotel.getId())
                            .name(hotel.getName())
                            .description(hotel.getDescription())
                            .address(hotel.getAddress())
                            .city(hotel.getCity())
                            .state(hotel.getState())
                            .country(hotel.getCountry())
                            .status(hotel.getStatus().name())
                            .minPricePerNight(minPrice != null ? minPrice : BigDecimal.ZERO)
                            .totalRoomTypes(totalTypes)
                            .totalAvailableRooms(availableRooms)
                            .ownerName(hotel.getOwner().getName())
                            .createdAt(hotel.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PagedResponse.<HotelSearchResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDetailResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));

        List<RoomResponse> rooms = roomRepository.findByHotelIdAndIsActiveTrue(hotel.getId())
                .stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());

        return HotelDetailResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .state(hotel.getState())
                .country(hotel.getCountry())
                .status(hotel.getStatus().name())
                .ownerId(hotel.getOwner().getId())
                .ownerName(hotel.getOwner().getName())
                .ownerEmail(hotel.getOwner().getEmail())
                .rooms(rooms)
                .createdAt(hotel.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getHotelRooms(Long hotelId) {
        // Verify hotel exists
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        return roomRepository.findByHotelIdAndIsActiveTrue(hotelId)
                .stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    // ──────────────── Admin Endpoints ────────────────

    @Override
    @Transactional
    public HotelDetailResponse createHotel(HotelCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User (owner)", request.getOwnerId()));

        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .owner(owner)
                .status(HotelStatus.PENDING)
                .build();

        Hotel saved = hotelRepository.save(hotel);
        return getHotelById(saved.getId());
    }

    @Override
    @Transactional
    public HotelDetailResponse updateHotel(Long id, HotelUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));

        // Update only the fields that are provided (partial update)
        if (request.getName() != null) hotel.setName(request.getName());
        if (request.getDescription() != null) hotel.setDescription(request.getDescription());
        if (request.getAddress() != null) hotel.setAddress(request.getAddress());
        if (request.getCity() != null) hotel.setCity(request.getCity());
        if (request.getState() != null) hotel.setState(request.getState());
        if (request.getCountry() != null) hotel.setCountry(request.getCountry());

        Hotel saved = hotelRepository.save(hotel);
        return getHotelById(saved.getId());
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));

        hotelRepository.delete(hotel);
    }

    @Override
    @Transactional
    public RoomResponse addRoom(Long hotelId, RoomCreateRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        Room room = Room.builder()
                .hotel(hotel)
                .roomType(request.getRoomType())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .pricePerNight(request.getPricePerNight())
                .totalRooms(request.getTotalRooms())
                .availableRooms(request.getTotalRooms()) // Initially all rooms are available
                .isActive(true)
                .build();

        Room saved = roomRepository.save(room);
        return mapToRoomResponse(saved);
    }

    // ──────────────── Mapper ────────────────

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotel().getId())
                .roomType(room.getRoomType())
                .description(room.getDescription())
                .capacity(room.getCapacity())
                .pricePerNight(room.getPricePerNight())
                .totalRooms(room.getTotalRooms())
                .availableRooms(room.getAvailableRooms())
                .isActive(room.getIsActive())
                .build();
    }
}
