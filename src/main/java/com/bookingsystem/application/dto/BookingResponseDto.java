package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDto {
    private Long bookingId;
    private Long classScheduleId;
    private String className;
    private BookingStatus status;
    private int remainingCredits; // remaining credits on the UserPackage used
}

