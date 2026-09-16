package com.courtflow.booking_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DailyAnalyticsDto {
    private LocalDate date;
    private long bookedSlots;
    private long totalSlotsCapacity;
    private double occupancyRate; // Tỷ lệ lấp đầy (%)
    private BigDecimal revenue;
}
