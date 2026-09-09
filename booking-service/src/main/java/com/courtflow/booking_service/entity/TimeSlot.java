package com.courtflow.booking_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // Ví dụ: 06:00:00

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // Ví dụ: 07:00:00
}
