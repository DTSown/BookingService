package com.courtflow.booking_service.dto;

import com.courtflow.booking_service.entity.Booking;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookingResponseDto {
    private Long bookingId;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private Booking.BookingStatus status;
    private LocalDateTime createdAt;
    private List<String> bookedSlots; // VD: ["08:00 - 09:00", "09:00 - 10:00"]
}