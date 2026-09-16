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
import com.courtflow.booking_service.repository.UserRepository;
import com.courtflow.booking_service.service.BookingService;
import com.courtflow.booking_service.service.PricingService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PricingService pricingService; // <-- Inject service tính giá động
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.getBookingDate().isBefore(today)) {
            throw new IllegalArgumentException("Không thể đặt sân cho ngày trong quá khứ!");
        }

        // Khóa bi quan (Pessimistic Lock) dòng sân để tránh race condition khi nhiều người đặt cùng lúc
        Court court = courtRepository.findByIdWithLock(request.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Sân không tồn tại với ID: " + request.getCourtId()));

        if (court.getStatus() != Court.CourtStatus.ACTIVE) {
            throw new IllegalStateException("Sân hiện đang bảo trì hoặc ngừng hoạt động!");
        }

        List<Booking.BookingStatus> activeStatuses = List.of(
                Booking.BookingStatus.PENDING,
                Booking.BookingStatus.CONFIRMED
        );

        boolean isToday = request.getBookingDate().isEqual(today);

        // Kiểm tra giờ quá khứ & trùng lịch
        for (Integer slotId : request.getTimeSlotIds()) {
            TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                    .orElseThrow(() -> new IllegalArgumentException("Khung giờ không hợp lệ: " + slotId));

            if (isToday && timeSlot.getStartTime().isBefore(now)) {
                throw new IllegalArgumentException(
                        "Không thể đặt khung giờ đã qua trong ngày hôm nay: " + timeSlot.getStartTime() + " - " + timeSlot.getEndTime()
                );
            }

            boolean isBooked = bookingDetailRepository.isSlotBooked(
                    court.getId(), request.getBookingDate(), slotId, activeStatuses
            );
            if (isBooked) {
                throw new SlotAlreadyBookedException(
                        "Khung giờ (" + timeSlot.getStartTime() + " - " + timeSlot.getEndTime() + ") vào ngày " + request.getBookingDate() + " đã có người đặt!"
                );
            }
        }

        // Lấy User đang đăng nhập
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("Bạn phải đăng nhập để đặt sân!");
        }
        String username = ((UserDetails) principal).getUsername();
        com.courtflow.booking_service.entity.User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng hiện tại"));

        // Khởi tạo booking trước
        Booking booking = Booking.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .totalAmount(BigDecimal.ZERO)
                .status(Booking.BookingStatus.PENDING)
                .user(currentUser)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> slotTimeStrings = new ArrayList<>();

        // Tính giá động cho từng slot theo ngày và giờ thực tế
        for (Integer slotId : request.getTimeSlotIds()) {
            TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                    .orElseThrow(() -> new IllegalArgumentException("Khung giờ không hợp lệ: " + slotId));

            // Tính giá động tại đây
            BigDecimal slotPrice = pricingService.calculateSlotPrice(request.getBookingDate(), timeSlot);
            totalAmount = totalAmount.add(slotPrice);

            BookingDetail detail = BookingDetail.builder()
                    .booking(booking)
                    .court(court)
                    .timeSlot(timeSlot)
                    .bookingDate(request.getBookingDate())
                    .price(slotPrice) // Lưu giá lịch sử lúc khách đặt
                    .build();

            booking.getDetails().add(detail);
            slotTimeStrings.add(timeSlot.getStartTime() + " - " + timeSlot.getEndTime() + " (" + slotPrice + "đ)");
        }

        booking.setTotalAmount(totalAmount);
        Booking savedBooking = bookingRepository.save(booking);

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

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt sân với mã: " + id));

        return mapToResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByPhone(String phone) {
        List<Booking> bookings = bookingRepository.findByCustomerPhoneOrderByCreatedAtDesc(phone);
        return bookings.stream().map(this::mapToResponseDto).toList();
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt sân với mã: " + id));

        // 1. Kiểm tra trạng thái: Không cho phép hủy đơn đã CONFIRMED, đã CANCELLED hoặc đã EXPIRED
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Đơn đặt đã được thanh toán và xác nhận (CONFIRMED), không thể hủy trực tiếp! Vui lòng liên hệ ban quản lý sân để được hỗ trợ.");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Đơn đặt này đã được hủy trước đó!");
        }
        if (booking.getStatus() == Booking.BookingStatus.EXPIRED) {
            throw new IllegalStateException("Đơn đặt đã hết hạn giữ chỗ, không thể hủy!");
        }

        // 2. Kiểm tra điều kiện thời gian hủy: Không cho hủy nếu khung giờ chơi đã qua
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        boolean hasPastSlot = booking.getDetails().stream()
                .anyMatch(detail -> detail.getBookingDate().isBefore(today)
                        || (detail.getBookingDate().isEqual(today) && detail.getTimeSlot().getStartTime().isBefore(now)));

        if (hasPastSlot) {
            throw new IllegalStateException("Không thể hủy đơn đặt sân cho khung giờ đã qua!");
        }

        // 3. Chuyển trạng thái sang CANCELLED (Slot sẽ tự động được giải phóng cho người khác)
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        return mapToResponseDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookings(Booking.BookingStatus status, LocalDate date, String phone) {
        String searchPhone = (phone != null && !phone.isBlank()) ? phone.trim() : null;
        List<Booking> bookings = bookingRepository.findAllWithDetailsAndFilters(status, date, searchPhone);
        return bookings.stream().map(this::mapToResponseDto).toList();
    }

    // Hàm tiện ích chuyển đổi Entity -> DTO
    private BookingResponseDto mapToResponseDto(Booking booking) {
        List<String> slotDetails = booking.getDetails().stream()
                .map(detail -> String.format("%s: %s - %s (Ngày: %s, Giá: %sđ)",
                        detail.getCourt().getName(),
                        detail.getTimeSlot().getStartTime(),
                        detail.getTimeSlot().getEndTime(),
                        detail.getBookingDate(),
                        detail.getPrice()))
                .toList();

        return BookingResponseDto.builder()
                .bookingId(booking.getId())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .bookedSlots(slotDetails)
                .build();
    }
}