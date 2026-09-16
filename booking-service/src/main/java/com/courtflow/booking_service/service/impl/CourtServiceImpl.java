package com.courtflow.booking_service.service.impl;

import com.courtflow.booking_service.dto.CourtAvailabilityResponseDto;
import com.courtflow.booking_service.dto.CourtResponseDto;
import com.courtflow.booking_service.dto.SlotAvailabilityDto;
import com.courtflow.booking_service.entity.Booking;
import com.courtflow.booking_service.entity.Court;
import com.courtflow.booking_service.entity.TimeSlot;
import com.courtflow.booking_service.repository.BookingDetailRepository;
import com.courtflow.booking_service.repository.CourtRepository;
import com.courtflow.booking_service.repository.TimeSlotRepository;
import com.courtflow.booking_service.service.CourtService;
import com.courtflow.booking_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PricingService pricingService;

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponseDto> getAllActiveCourts() {
        return courtRepository.findByStatus(Court.CourtStatus.ACTIVE)
                .stream()
                .map(court -> CourtResponseDto.builder()
                        .id(court.getId())
                        .name(court.getName())
                        .status(court.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtAvailabilityResponseDto> getCourtAvailability(LocalDate date) {
        // 1. Lấy danh sách sân đang hoạt động và danh sách khung giờ sắp xếp theo giờ bắt đầu
        List<Court> activeCourts = courtRepository.findByStatus(Court.CourtStatus.ACTIVE);
        List<TimeSlot> allSlots = timeSlotRepository.findAll(Sort.by(Sort.Direction.ASC, "startTime"));

        List<Booking.BookingStatus> activeStatuses = List.of(
                Booking.BookingStatus.PENDING,
                Booking.BookingStatus.CONFIRMED
        );

        List<CourtAvailabilityResponseDto> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();

        // 2. Với mỗi sân, tổng hợp danh sách slot cùng trạng thái còn trống hay không
        for (Court court : activeCourts) {
            Set<Integer> bookedSlotIds = new HashSet<>(
                    bookingDetailRepository.findBookedSlotIds(court.getId(), date, activeStatuses)
            );

            List<SlotAvailabilityDto> slotDtos = new ArrayList<>();

            for (TimeSlot slot : allSlots) {
                boolean isPast = date.isBefore(today) || (date.isEqual(today) && slot.getStartTime().isBefore(now));
                boolean isAvailable = !bookedSlotIds.contains(slot.getId()) && !isPast;

                slotDtos.add(SlotAvailabilityDto.builder()
                        .slotId(slot.getId())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .price(pricingService.calculateSlotPrice(date, slot))
                        .available(isAvailable)
                        .build());
            }

            result.add(CourtAvailabilityResponseDto.builder()
                    .courtId(court.getId())
                    .courtName(court.getName())
                    .queryDate(date)
                    .slots(slotDtos)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponseDto> getAllCourts() {
        return courtRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::mapToCourtResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourtResponseDto getCourtById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân với ID: " + id));
        return mapToCourtResponseDto(court);
    }

    @Override
    @Transactional
    public CourtResponseDto createCourt(com.courtflow.booking_service.dto.CourtRequestDto request) {
        String trimmedName = request.getName().trim();
        if (courtRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Tên sân '" + trimmedName + "' đã tồn tại trong hệ thống!");
        }

        Court court = Court.builder()
                .name(trimmedName)
                .status(request.getStatus() != null ? request.getStatus() : Court.CourtStatus.ACTIVE)
                .build();

        Court savedCourt = courtRepository.save(court);
        return mapToCourtResponseDto(savedCourt);
    }

    @Override
    @Transactional
    public CourtResponseDto updateCourtStatus(Long id, Court.CourtStatus newStatus) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân với ID: " + id));

        court.setStatus(newStatus);
        Court updated = courtRepository.save(court);
        return mapToCourtResponseDto(updated);
    }

    @Override
    @Transactional
    public CourtResponseDto updateCourt(Long id, com.courtflow.booking_service.dto.CourtRequestDto request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân với ID: " + id));

        String trimmedName = request.getName().trim();
        if (courtRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new IllegalArgumentException("Tên sân '" + trimmedName + "' đã được sử dụng bởi sân khác!");
        }

        court.setName(trimmedName);
        if (request.getStatus() != null) {
            court.setStatus(request.getStatus());
        }

        Court updated = courtRepository.save(court);
        return mapToCourtResponseDto(updated);
    }

    private CourtResponseDto mapToCourtResponseDto(Court court) {
        return CourtResponseDto.builder()
                .id(court.getId())
                .name(court.getName())
                .status(court.getStatus())
                .build();
    }
}