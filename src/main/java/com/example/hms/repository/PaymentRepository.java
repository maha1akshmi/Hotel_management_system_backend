package com.example.hms.repository;

import com.example.hms.entity.Payment;
import com.example.hms.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    long countByStatus(PaymentStatus status);
}
