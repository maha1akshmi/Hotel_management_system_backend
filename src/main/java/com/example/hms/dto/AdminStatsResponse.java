package com.example.hms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long bannedUsers;

    private long totalHotels;
    private long pendingHotels;
    private long approvedHotels;

    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;

    private BigDecimal totalRevenue;

    private long totalPayments;
    private long successfulPayments;

    // Recent bookings for a quick dashboard preview
    private List<BookingResponse> recentBookings;
}
