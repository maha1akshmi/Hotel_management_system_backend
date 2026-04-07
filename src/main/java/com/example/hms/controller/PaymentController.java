package com.example.hms.controller;

import com.example.hms.dto.ApiResponse;
import com.example.hms.dto.CreatePaymentRequest;
import com.example.hms.dto.PaymentResponse;
import com.example.hms.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Process and view payment status")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Process payment", description = "Creates a payment for a confirmed booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        String email = authentication.getName();
        PaymentResponse payment = paymentService.processPayment(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "Payment processed successfully"));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get payment status", description = "Returns payment info for a specific booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingId(
            Authentication authentication,
            @PathVariable Long bookingId
    ) {
        String email = authentication.getName();
        PaymentResponse payment = paymentService.getPaymentByBookingId(email, bookingId);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }
}
