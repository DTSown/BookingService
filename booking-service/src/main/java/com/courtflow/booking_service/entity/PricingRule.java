package com.courtflow.booking_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20)
    private DayType dayType; // WEEKDAY (Thứ 2 - Thứ 6), WEEKEND (Thứ 7, CN), ALL

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "price_per_slot", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerSlot;

    @Column(length = 100)
    private String description; // Ví dụ: "Giờ vàng ngày thường", "Cuối tuần"

    public enum DayType {
        WEEKDAY,
        WEEKEND,
        ALL
    }
}