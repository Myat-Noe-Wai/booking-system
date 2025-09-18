package com.bookingsystem.application.dto;

import com.bookingsystem.application.enums.Country;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PackageDto {
    private Long id;
    private String packageName;
    private int credits;
    private double price;
    private Country country;
    private LocalDate expiryDate;
}

