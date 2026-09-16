package com.courtflow.booking_service.service;

import com.courtflow.booking_service.entity.TimeSlot;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingService {
    BigDecimal calculateSlotPrice(LocalDate date, TimeSlot timeSlot);
}