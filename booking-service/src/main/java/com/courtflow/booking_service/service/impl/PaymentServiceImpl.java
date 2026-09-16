package com.courtflow.booking_service.service.impl;

import com.courtflow.booking_service.dto.PaymentRequestDto;
import com.courtflow.booking_service.dto.PaymentResponseDto;
import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.entity.Payment;
import com.courtflow.booking_service.repository.BookingRepository;
import com.courtflow.booking_service.repository.PaymentRepository;
import com.courtflow.booking_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        // 1. Tìm đơn booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt sân với ID: " + request.getBookingId()));

        // 2. Kiểm tra trạng thái đơn: Phải là PENDING mới cho thanh toán
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Đơn đặt này đã được thanh toán trước đó!");
        }

        if (booking.getStatus() == Booking.BookingStatus.EXPIRED) {
            throw new IllegalStateException("Đơn đặt đã hết thời hạn giữ chỗ (10 phút). Slot đã được giải phóng!");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Đơn đặt đã bị hủy, không thể thanh toán!");
        }

        // 3. Giả lập sinh mã Transaction ID nếu client không truyền
        String txId = (request.getTransactionId() != null && !request.getTransactionId().isBlank())
                ? request.getTransactionId()
                : "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Kiểm tra tránh trùng lặp Transaction ID (Idempotency)
        if (paymentRepository.existsByTransactionId(txId)) {
            throw new IllegalStateException("Giao dịch này đã được xử lý trước đó!");
        }

        // 4. Lưu bản ghi Payment (Ghi nhận thanh toán thành công)
        Payment payment = Payment.builder()
                .booking(booking)
                .transactionId(txId)
                .amount(booking.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.SUCCESS)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 5. Cập nhật trạng thái Booking sang CONFIRMED
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // 6. Trả về kết quả
        return PaymentResponseDto.builder()
                .paymentId(savedPayment.getId())
                .bookingId(booking.getId())
                .transactionId(savedPayment.getTransactionId())
                .amount(savedPayment.getAmount())
                .paymentMethod(savedPayment.getPaymentMethod())
                .paymentStatus(savedPayment.getStatus())
                .bookingStatus(booking.getStatus().name())
                .paymentTime(savedPayment.getPaymentTime())
                .build();
    }
}