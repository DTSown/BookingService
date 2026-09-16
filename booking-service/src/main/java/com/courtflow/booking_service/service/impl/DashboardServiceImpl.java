package com.courtflow.booking_service.service.impl;

import com.courtflow.booking_service.dto.CourtOccupancyDto;
import com.courtflow.booking_service.dto.DailyAnalyticsDto;
import com.courtflow.booking_service.dto.DashboardAnalyticsResponseDto;
import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.entity.BookingDetail;
import com.courtflow.booking_service.entity.Court;
import com.courtflow.booking_service.repository.BookingDetailRepository;
import com.courtflow.booking_service.repository.CourtRepository;
import com.courtflow.booking_service.repository.TimeSlotRepository;
import com.courtflow.booking_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingDetailRepository bookingDetailRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardAnalyticsResponseDto getDashboardAnalytics(LocalDate startDate, LocalDate endDate) {
        // 1. Chuẩn hóa khoảng thời gian: Mặc định là 7 ngày gần nhất nếu không truyền
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(6);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày kết thúc (" + end + ") không thể trước ngày bắt đầu (" + start + ")!");
        }

        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;

        // 2. Lấy thông tin tài nguyên sân và slot
        List<Court> allCourts = courtRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        long totalCourts = allCourts.size();
        long totalSlotsPerDay = timeSlotRepository.count();

        long capacityPerCourt = totalSlotsPerDay * totalDays;
        long dailyCapacityAllCourts = totalCourts * totalSlotsPerDay;
        long totalCapacitySlots = totalCourts * totalSlotsPerDay * totalDays;

        // 3. Lấy tất cả các chi tiết đơn đặt đã CONFIRMED trong khoảng thời gian này
        List<BookingDetail> confirmedDetails = bookingDetailRepository.findConfirmedDetailsBetweenDates(
                start, end, Booking.BookingStatus.CONFIRMED
        );

        long totalBookedSlots = confirmedDetails.size();
        BigDecimal totalRevenue = confirmedDetails.stream()
                .map(BookingDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalConfirmedBookings = confirmedDetails.stream()
                .map(detail -> detail.getBooking().getId())
                .distinct()
                .count();

        double overallOccupancyRate = (totalCapacitySlots > 0)
                ? roundTwoDecimals(((double) totalBookedSlots / totalCapacitySlots) * 100.0)
                : 0.0;

        // 4. Nhóm thống kê theo từng sân (Court Analytics)
        Map<Long, List<BookingDetail>> detailsByCourt = confirmedDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getCourt().getId()));

        List<CourtOccupancyDto> courtAnalytics = new ArrayList<>();
        for (Court court : allCourts) {
            List<BookingDetail> courtDetails = detailsByCourt.getOrDefault(court.getId(), Collections.emptyList());
            long courtBookedSlots = courtDetails.size();
            BigDecimal courtRevenue = courtDetails.stream()
                    .map(BookingDetail::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double courtOccupancy = (capacityPerCourt > 0)
                    ? roundTwoDecimals(((double) courtBookedSlots / capacityPerCourt) * 100.0)
                    : 0.0;

            courtAnalytics.add(CourtOccupancyDto.builder()
                    .courtId(court.getId())
                    .courtName(court.getName())
                    .bookedSlots(courtBookedSlots)
                    .totalSlotsCapacity(capacityPerCourt)
                    .occupancyRate(courtOccupancy)
                    .revenue(courtRevenue)
                    .build());
        }

        // 5. Nhóm thống kê theo từng ngày (Daily Analytics)
        Map<LocalDate, List<BookingDetail>> detailsByDate = confirmedDetails.stream()
                .collect(Collectors.groupingBy(BookingDetail::getBookingDate));

        List<DailyAnalyticsDto> dailyAnalytics = new ArrayList<>();
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            List<BookingDetail> dayDetails = detailsByDate.getOrDefault(currentDate, Collections.emptyList());
            long dayBookedSlots = dayDetails.size();
            BigDecimal dayRevenue = dayDetails.stream()
                    .map(BookingDetail::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double dayOccupancy = (dailyCapacityAllCourts > 0)
                    ? roundTwoDecimals(((double) dayBookedSlots / dailyCapacityAllCourts) * 100.0)
                    : 0.0;

            dailyAnalytics.add(DailyAnalyticsDto.builder()
                    .date(currentDate)
                    .bookedSlots(dayBookedSlots)
                    .totalSlotsCapacity(dailyCapacityAllCourts)
                    .occupancyRate(dayOccupancy)
                    .revenue(dayRevenue)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return DashboardAnalyticsResponseDto.builder()
                .startDate(start)
                .endDate(end)
                .totalDays(totalDays)
                .totalRevenue(totalRevenue)
                .totalConfirmedBookings(totalConfirmedBookings)
                .totalBookedSlots(totalBookedSlots)
                .totalCapacitySlots(totalCapacitySlots)
                .overallOccupancyRate(overallOccupancyRate)
                .courtAnalytics(courtAnalytics)
                .dailyAnalytics(dailyAnalytics)
                .build();
    }

    private double roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
