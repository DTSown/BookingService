package com.courtflow.booking_service.controller;

import com.courtflow.booking_service.dto.BookingResponseDto;
import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    // 1. Xem toàn bộ đơn đặt và lọc theo trạng thái, ngày chơi, hoặc số điện thoại
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings(
            @RequestParam(name = "status", required = false) Booking.BookingStatus status,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "phone", required = false) String phone
    ) {
        return ResponseEntity.ok(bookingService.getAllBookings(status, date, phone));
    }

    // 2. Xem chi tiết 1 đơn đặt sân
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }
}
