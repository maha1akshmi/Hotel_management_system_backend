package com.example.hms.service;

import com.example.hms.dto.*;
import com.example.hms.entity.*;
import com.example.hms.enums.*;
import com.example.hms.exception.ResourceNotFoundException;
import com.example.hms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        // Gather recent bookings for dashboard preview
        List<BookingResponse> recentBookings = bookingRepository.findTop10ByOrderByBookedAtDesc()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsActiveTrue())
                .bannedUsers(userRepository.countByIsActiveFalse())
                .totalHotels(hotelRepository.count())
                .pendingHotels(hotelRepository.countByStatus(HotelStatus.PENDING))
                .approvedHotels(hotelRepository.countByStatus(HotelStatus.APPROVED))
                .totalBookings(bookingRepository.count())
                .confirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED))
                .cancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED))
                .totalRevenue(bookingRepository.sumTotalRevenue())
                .totalPayments(paymentRepository.count())
                .successfulPayments(paymentRepository.countByStatus(PaymentStatus.SUCCESS))
                .recentBookings(recentBookings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public UserResponse toggleBanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Toggle the active status (ban/unban)
        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HotelResponse> getAllHotels(Pageable pageable) {
        Page<Hotel> page = hotelRepository.findAll(pageable);
        List<HotelResponse> content = page.getContent().stream()
                .map(this::mapToHotelResponse)
                .collect(Collectors.toList());

        return PagedResponse.<HotelResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public HotelResponse approveHotel(Long hotelId, String status) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        // Validate and set the new status
        try {
            HotelStatus newStatus = HotelStatus.valueOf(status.toUpperCase());
            hotel.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hotel status: " + status
                    + ". Must be one of: PENDING, APPROVED, REJECTED");
        }

        Hotel saved = hotelRepository.save(hotel);
        return mapToHotelResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> page = bookingRepository.findAll(pageable);
        List<BookingResponse> content = page.getContent().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return PagedResponse.<BookingResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ──────────────── Mappers ────────────────

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .oauthProvider(user.getOauthProvider())
                .isVerified(user.getIsVerified())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private HotelResponse mapToHotelResponse(Hotel hotel) {
        long rooms = roomRepository.countByHotelId(hotel.getId());

        return HotelResponse.builder()
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
                .roomCount(rooms)
                .createdAt(hotel.getCreatedAt())
                .build();
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        // Lookup payment info if it exists
        Optional<Payment> payment = paymentRepository.findByBookingId(booking.getId());

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .userEmail(booking.getUser().getEmail())
                .hotelId(booking.getHotel().getId())
                .hotelName(booking.getHotel().getName())
                .hotelCity(booking.getHotel().getCity())
                .roomId(booking.getRoom().getId())
                .roomType(booking.getRoom().getRoomType())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guests(booking.getGuests())
                .roomsBooked(booking.getRoomsBooked())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name())
                .paymentStatus(payment.map(p -> p.getStatus().name()).orElse("UNPAID"))
                .paymentMethod(payment.map(Payment::getMethod).orElse(null))
                .bookedAt(booking.getBookedAt())
                .build();
    }
}
