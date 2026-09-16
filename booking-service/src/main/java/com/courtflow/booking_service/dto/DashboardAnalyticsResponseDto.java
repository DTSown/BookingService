package com.courtflow.booking_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardAnalyticsResponseDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private BigDecimal totalRevenue;
    private long totalConfirmedBookings;
    private long totalBookedSlots;
    private long totalCapacitySlots;
    private double overallOccupancyRate; // Tỷ lệ lấp đầy toàn hệ thống (%)
    private List<CourtOccupancyDto> courtAnalytics;
    private List<DailyAnalyticsDto> dailyAnalytics;
}
