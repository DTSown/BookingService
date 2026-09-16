package com.courtflow.booking_service.dto;

import com.courtflow.booking_service.entity.Court;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourtStatusUpdateRequestDto {

    @NotNull(message = "Trạng thái sân không được để trống (ACTIVE hoặc MAINTENANCE)")
    private Court.CourtStatus status;
}
