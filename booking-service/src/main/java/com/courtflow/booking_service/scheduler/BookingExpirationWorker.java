package com.courtflow.booking_service.scheduler;

import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationWorker {

    private final BookingRepository bookingRepository;

    // Thời gian giữ chỗ: 10 phút
    private static final long HOLD_TIME_MINUTES = 1;

    /**
     * Chạy định kỳ mỗi 60 giây (fixedRate = 60000 ms)
     * Quét và hủy các đơn giữ chỗ chưa thanh toán quá 10 phút
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void autoExpireOverdueBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(HOLD_TIME_MINUTES);

        int updatedCount = bookingRepository.expirePendingBookings(
                Booking.BookingStatus.PENDING,
                Booking.BookingStatus.EXPIRED,
                cutoffTime
        );

        if (updatedCount > 0) {
            log.info(" Đã tự động giải phóng {} đơn đặt sân quá hạn thanh toán (> 10 phút)", updatedCount);
        }
    }
}