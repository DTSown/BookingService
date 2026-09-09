package com.courtflow.booking_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BookingRequestDto {

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String customerPhone;

    @NotNull(message = "Ngày đặt không được để trống")
    @FutureOrPresent(message = "Ngày đặt phải là hôm nay hoặc tương lai")
    private LocalDate bookingDate;

    @NotNull(message = "Vui lòng chọn sân")
    private Long courtId;

    @NotEmpty(message = "Vui lòng chọn ít nhất một khung giờ")
    private List<Integer> timeSlotIds; // Danh sách ID các slot khách muốn đặt
}