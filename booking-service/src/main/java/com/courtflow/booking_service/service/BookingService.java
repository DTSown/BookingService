package com.courtflow.booking_service.service;

import com.courtflow.booking_service.dto.BookingRequestDto;
import com.courtflow.booking_service.dto.BookingResponseDto;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto request);
}