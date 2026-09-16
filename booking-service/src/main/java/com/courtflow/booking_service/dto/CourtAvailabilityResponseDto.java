package com.courtflow.booking_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CourtAvailabilityResponseDto {
    private Long courtId;
    private String courtName;
    private LocalDate queryDate;
    private List<SlotAvailabilityDto> slots;
}