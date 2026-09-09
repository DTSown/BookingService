package com.courtflow.booking_service.service.impl;

import com.courtflow.booking_service.dto.BookingRequestDto;
import com.courtflow.booking_service.dto.BookingResponseDto;
import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.entity.BookingDetail;
import com.courtflow.booking_service.entity.Court;
import com.courtflow.booking_service.entity.TimeSlot;
import com.courtflow.booking_service.exception.SlotAlreadyBookedException;
import com.courtflow.booking_service.repository.BookingDetailRepository;
import com.courtflow.booking_service.repository.BookingRepository;
import com.courtflow.booking_service.repository.CourtRepository;
import com.courtflow.booking_service.repository.TimeSlotRepository;
import com.courtflow.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    // Giả định giá mặc định 100.000đ/slot (sẽ nâng cấp tính giá động sau)
    private static final BigDecimal DEFAULT_SLOT_PRICE = new BigDecimal("100000.00");

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {
        // 1. Kiểm tra sân có tồn tại và đang hoạt động không
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Sân không tồn tại với ID: " + request.getCourtId()));

        if (court.getStatus() != Court.CourtStatus.ACTIVE) {
            throw new IllegalStateException("Sân hiện đang bảo trì hoặc ngừng hoạt động!");
        }

        // Danh sách trạng thái được coi là slot đã bị chiếm chỗ
        List<Booking.BookingStatus> activeStatuses = List.of(
                Booking.BookingStatus.PENDING,
                Booking.BookingStatus.CONFIRMED
        );

        // 2. Kiểm tra trùng lịch cho từng slot trước khi thực hiện lưu
        for (Integer slotId : request.getTimeSlotIds()) {
            boolean isBooked = bookingDetailRepository.isSlotBooked(
                    court.getId(),
                    request.getBookingDate(),
                    slotId,
                    activeStatuses
            );

            if (isBooked) {
                throw new SlotAlreadyBookedException(
                        "Khung giờ (Slot ID: " + slotId + ") vào ngày " + request.getBookingDate() + " đã có người đặt!"
                );
            }
        }

        // 3. Khởi tạo đơn đặt hàng (Booking)
        BigDecimal totalAmount = DEFAULT_SLOT_PRICE.multiply(BigDecimal.valueOf(request.getTimeSlotIds().size()));

        Booking booking = Booking.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .totalAmount(totalAmount)
                .status(Booking.BookingStatus.PENDING)
                .build();

        // 4. Tạo chi tiết đơn đặt (BookingDetail) cho từng khung giờ
        List<String> slotTimeStrings = new ArrayList<>();

        for (Integer slotId : request.getTimeSlotIds()) {
            TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                    .orElseThrow(() -> new IllegalArgumentException("Khung giờ không hợp lệ: " + slotId));

            BookingDetail detail = BookingDetail.builder()
                    .booking(booking)
                    .court(court)
                    .timeSlot(timeSlot)
                    .bookingDate(request.getBookingDate())
                    .price(DEFAULT_SLOT_PRICE)
                    .build();

            booking.getDetails().add(detail);
            slotTimeStrings.add(timeSlot.getStartTime() + " - " + timeSlot.getEndTime());
        }

        // 5. Lưu vào Database (Nhờ cascade = ALL trên Booking nên BookingDetail sẽ tự động được lưu)
        Booking savedBooking = bookingRepository.save(booking);

        // 6. Trả về kết quả
        return BookingResponseDto.builder()
                .bookingId(savedBooking.getId())
                .customerName(savedBooking.getCustomerName())
                .customerPhone(savedBooking.getCustomerPhone())
                .totalAmount(savedBooking.getTotalAmount())
                .status(savedBooking.getStatus())
                .createdAt(savedBooking.getCreatedAt())
                .bookedSlots(slotTimeStrings)
                .build();
    }
}