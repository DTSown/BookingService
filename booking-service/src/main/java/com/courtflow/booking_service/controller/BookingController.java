package com.courtflow.booking_service.controller;

import com.courtflow.booking_service.dto.BookingRequestDto;
import com.courtflow.booking_service.dto.BookingResponseDto;
import com.courtflow.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 1. Tạo đơn đặt giữ chỗ (POST)
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingRequestDto request) {
        BookingResponseDto response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Tra cứu chi tiết đơn bằng Booking ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // 3. Tra cứu lịch sử đặt bằng Số điện thoại (GET /api/v1/bookings?phone=0987654321)
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookingsByPhone(@RequestParam("phone") String phone) {
        return ResponseEntity.ok(bookingService.getBookingsByPhone(phone));
    }

    // 4. Hủy đơn đặt sân
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}