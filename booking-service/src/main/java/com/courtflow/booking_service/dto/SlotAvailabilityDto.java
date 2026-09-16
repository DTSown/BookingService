package com.courtflow.booking_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Builder
public class SlotAvailabilityDto {
    private Integer slotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private boolean available; 
}