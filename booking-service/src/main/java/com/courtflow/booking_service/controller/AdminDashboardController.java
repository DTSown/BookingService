package com.courtflow.booking_service.controller;

import com.courtflow.booking_service.dto.DashboardAnalyticsResponseDto;
import com.courtflow.booking_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    // Báo cáo Dashboard: Tổng doanh thu và tỷ lệ lấp đầy sân theo khoảng thời gian
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAnalyticsResponseDto> getDashboardAnalytics(
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getDashboardAnalytics(startDate, endDate));
    }
}
