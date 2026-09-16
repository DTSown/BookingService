package com.courtflow.booking_service.dto;

import com.courtflow.booking_service.entity.Court;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourtResponseDto {
    private Long id;
    private String name;
    private Court.CourtStatus status;
}