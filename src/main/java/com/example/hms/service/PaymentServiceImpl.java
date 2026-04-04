package com.example.hms.service;

import com.example.hms.dto.CreatePaymentRequest;
import com.example.hms.dto.PaymentResponse;
import com.example.hms.entity.Booking;
import com.example.hms.entity.Payment;
import com.example.hms.exception.BadRequestException;
import com.example.hms.exception.ResourceNotFoundException;
import com.example.hms.exception.UnauthorizedException;
import com.example.hms.repository.BookingRepository;
import com.example.hms.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(String userEmail, CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        // Ensure the booking belongs to the requesting user
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You are not authorized to pay for this booking");
        }

        // Can only pay for confirmed bookings
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Can only pay for confirmed bookings. Current status: " + booking.getStatus());
        }

        // Check if payment already exists
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BadRequestException("Payment has already been made for this booking");
        }

        // Validate payment method
        String method = request.getMethod().toUpperCase();
        if (!method.equals("CARD") && !method.equals("UPI") && !method.equals("NET_BANKING")) {
            throw new BadRequestException("Invalid payment method. Allowed: CARD, UPI, NET_BANKING");
        }

        // Simulate payment processing — generate a transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .method(method)
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    @Override
    public PaymentResponse getPaymentByBookingId(String userEmail, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        // Ensure the booking belongs to the requesting user
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You are not authorized to view this payment");
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for booking ID: " + bookingId));

        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
