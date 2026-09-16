package com.courtflow.booking_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CourtOccupancyDto {
    private Long courtId;
    private String courtName;
    private long bookedSlots;
    private long totalSlotsCapacity;
    private double occupancyRate; // Tỷ lệ lấp đầy (%)
    private BigDecimal revenue;
}
