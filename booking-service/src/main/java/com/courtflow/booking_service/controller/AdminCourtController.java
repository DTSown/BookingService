package com.courtflow.booking_service.controller;

import com.courtflow.booking_service.dto.CourtRequestDto;
import com.courtflow.booking_service.dto.CourtResponseDto;
import com.courtflow.booking_service.dto.CourtStatusUpdateRequestDto;
import com.courtflow.booking_service.service.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/courts")
@RequiredArgsConstructor
public class AdminCourtController {

    private final CourtService courtService;

    // 1. Lấy tất cả danh sách sân (cả ACTIVE lẫn MAINTENANCE)
    @GetMapping
    public ResponseEntity<List<CourtResponseDto>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    // 2. Lấy chi tiết sân theo ID
    @GetMapping("/{id}")
    public ResponseEntity<CourtResponseDto> getCourtById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    // 3. Thêm sân mới
    @PostMapping
    public ResponseEntity<CourtResponseDto> createCourt(@Valid @RequestBody CourtRequestDto request) {
        CourtResponseDto response = courtService.createCourt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. Đổi trạng thái bảo trì / hoạt động (PATCH /api/v1/admin/courts/{id}/status)
    @PatchMapping("/{id}/status")
    public ResponseEntity<CourtResponseDto> updateCourtStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourtStatusUpdateRequestDto request) {
        return ResponseEntity.ok(courtService.updateCourtStatus(id, request.getStatus()));
    }

    // 5. Cập nhật thông tin sân (Tên & Trạng thái)
    @PutMapping("/{id}")
    public ResponseEntity<CourtResponseDto> updateCourt(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourtRequestDto request) {
        return ResponseEntity.ok(courtService.updateCourt(id, request));
    }
}
