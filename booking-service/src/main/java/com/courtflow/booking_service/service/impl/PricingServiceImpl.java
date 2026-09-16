package com.courtflow.booking_service.service.impl;

import com.courtflow.booking_service.entity.PricingRule;
import com.courtflow.booking_service.entity.TimeSlot;
import com.courtflow.booking_service.repository.PricingRuleRepository;
import com.courtflow.booking_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private static final BigDecimal FALLBACK_PRICE = new BigDecimal("100000.00");

    @Override
    public BigDecimal calculateSlotPrice(LocalDate date, TimeSlot timeSlot) {
        // 1. Xác định ngày là Cuối tuần (T7, CN) hay Ngày thường (T2 - T6)
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        PricingRule.DayType dayType = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
                ? PricingRule.DayType.WEEKEND
                : PricingRule.DayType.WEEKDAY;

        // 2. Tìm bảng giá khớp với loại ngày và khung giờ
        return pricingRuleRepository.findMatchingRule(dayType, timeSlot.getStartTime())
                .map(PricingRule::getPricePerSlot)
                .orElse(FALLBACK_PRICE); // Nếu chưa cấu hình luật thì lấy giá mặc định
    }
}