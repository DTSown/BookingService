package com.courtflow.booking_service.service;

import com.courtflow.booking_service.dto.BookingRequestDto;
import com.courtflow.booking_service.dto.BookingResponseDto;
import com.courtflow.booking_service.entity.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    // User APIs
    BookingResponseDto createBooking(BookingRequestDto request);
    BookingResponseDto getBookingById(Long id);
    List<BookingResponseDto> getBookingsByPhone(String phone);
    BookingResponseDto cancelBooking(Long id);

    // Admin Operations
    List<BookingResponseDto> getAllBookings(Booking.BookingStatus status, LocalDate date, String phone);
}