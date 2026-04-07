package com.example.hms.service;

import com.example.hms.dto.BookingResponse;
import com.example.hms.dto.CreateBookingRequest;
import com.example.hms.entity.*;
import com.example.hms.enums.BookingStatus;
import com.example.hms.exception.BadRequestException;
import com.example.hms.exception.ResourceNotFoundException;
import com.example.hms.exception.UnauthorizedException;
import com.example.hms.repository.BookingRepository;
import com.example.hms.repository.RoomRepository;
import com.example.hms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(String userEmail, CreateBookingRequest request) {
        // Validate dates
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get room (includes hotel via FK)
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        // Validate hotel ID matches room's hotel
        if (!room.getHotel().getId().equals(request.getHotelId())) {
            throw new BadRequestException("Room does not belong to the specified hotel");
        }

        // Validate room is active
        if (!room.getIsActive()) {
            throw new BadRequestException("This room is currently unavailable");
        }

        // Check guest capacity
        if (request.getGuests() > room.getCapacity() * request.getRoomsBooked()) {
            throw new BadRequestException("Number of guests exceeds total room capacity. Max capacity: "
                    + (room.getCapacity() * request.getRoomsBooked()));
        }

        // Check room availability — count overlapping non-cancelled bookings
        List<Booking> overlapping = bookingRepository
                .findByRoomIdAndStatusNotAndCheckInLessThanAndCheckOutGreaterThan(
                        room.getId(),
                        BookingStatus.CANCELLED,
                        request.getCheckOut(),
                        request.getCheckIn()
                );

        int bookedRooms = overlapping.stream()
                .mapToInt(Booking::getRoomsBooked)
                .sum();

        int availableRooms = room.getTotalRooms() - bookedRooms;
        if (request.getRoomsBooked() > availableRooms) {
            throw new BadRequestException("Not enough rooms available. Only " + availableRooms + " room(s) left for these dates.");
        }

        // Calculate total price: pricePerNight × nights × roomsBooked
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        BigDecimal totalPrice = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.getRoomsBooked()));

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .hotel(room.getHotel())
                .room(room)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .guests(request.getGuests())
                .roomsBooked(request.getRoomsBooked())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    @Override
    public List<BookingResponse> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUserIdOrderByBookedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getBookingById(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Ensure the booking belongs to the requesting user
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You are not authorized to view this booking");
        }

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Ensure the booking belongs to the requesting user
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You are not authorized to cancel this booking");
        }

        // Can only cancel CONFIRMED bookings
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed bookings can be cancelled. Current status: " + booking.getStatus());
        }

        // Cannot cancel past bookings
        if (booking.getCheckIn().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot cancel a booking that has already started");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    private BookingResponse mapToResponse(Booking booking) {
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
                .bookedAt(booking.getBookedAt())
                .build();
    }
}
