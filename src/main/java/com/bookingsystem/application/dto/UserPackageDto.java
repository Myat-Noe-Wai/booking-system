package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.PackageStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserPackageDto {
    private Long id;
    private String packageName;
    private int remainingCredits;
    private LocalDate expiryDate;
    private PackageStatus packageStatus;
}

