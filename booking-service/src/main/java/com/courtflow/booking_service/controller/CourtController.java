package com.courtflow.booking_service.controller;

import com.courtflow.booking_service.dto.CourtAvailabilityResponseDto;
import com.courtflow.booking_service.dto.CourtResponseDto;
import com.courtflow.booking_service.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // 1. Lấy danh sách sân
    @GetMapping
    public ResponseEntity<List<CourtResponseDto>> getCourts() {
        return ResponseEntity.ok(courtService.getAllActiveCourts());
    }

    // 2. Xem ma trận lịch trống & giá theo ngày cụ thể (mặc định lấy ngày hôm nay nếu không truyền)
    @GetMapping("/availability")
    public ResponseEntity<List<CourtAvailabilityResponseDto>> checkAvailability(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(courtService.getCourtAvailability(queryDate));
    }
}