package com.courtflow.booking_service.service;

import com.courtflow.booking_service.dto.DashboardAnalyticsResponseDto;

import java.time.LocalDate;

public interface DashboardService {
    DashboardAnalyticsResponseDto getDashboardAnalytics(LocalDate startDate, LocalDate endDate);
}
