package com.courtflow.booking_service.service;

import com.courtflow.booking_service.dto.CourtAvailabilityResponseDto;
import com.courtflow.booking_service.dto.CourtRequestDto;
import com.courtflow.booking_service.dto.CourtResponseDto;
import com.courtflow.booking_service.entity.Court;

import java.time.LocalDate;
import java.util.List;

public interface CourtService {
    // User APIs
    List<CourtResponseDto> getAllActiveCourts();
    List<CourtAvailabilityResponseDto> getCourtAvailability(LocalDate date);

    // Admin APIs
    List<CourtResponseDto> getAllCourts();
    CourtResponseDto getCourtById(Long id);
    CourtResponseDto createCourt(CourtRequestDto request);
    CourtResponseDto updateCourtStatus(Long id, Court.CourtStatus newStatus);
    CourtResponseDto updateCourt(Long id, CourtRequestDto request);
}