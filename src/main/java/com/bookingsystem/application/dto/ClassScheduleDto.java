package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.Country;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClassScheduleDto {
    private Long id;
    private String className;
    private Country country;
    private int requiredCredits;
    private int totalSlots;
    private int bookedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

