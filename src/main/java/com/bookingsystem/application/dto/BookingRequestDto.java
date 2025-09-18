package com.bookingsystem.application.dto;

import lombok.Data;

@Data
public class BookingRequestDto {
    private Long classScheduleId;
    private Long userPackageId; // which package to use (required)
}

