package com.example.hms.service;

import com.example.hms.dto.CreatePaymentRequest;
import com.example.hms.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(String userEmail, CreatePaymentRequest request);
    PaymentResponse getPaymentByBookingId(String userEmail, Long bookingId);
}
