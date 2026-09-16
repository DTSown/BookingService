package com.courtflow.booking_service.dto;

import com.courtflow.booking_service.entity.Court;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourtRequestDto {

    @NotBlank(message = "Tên sân không được để trống")
    private String name;

    private Court.CourtStatus status = Court.CourtStatus.ACTIVE;
}
