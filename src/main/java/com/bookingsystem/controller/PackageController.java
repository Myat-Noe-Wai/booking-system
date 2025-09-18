package com.bookingsystem.controller;

import com.bookingsystem.application.dto.PackageDto;
import com.bookingsystem.application.dto.UserPackageDto;
import com.bookingsystem.application.enums.Country;
import com.bookingsystem.application.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    // Get available packages for a country
    @GetMapping("/available/{country}")
    public List<PackageDto> getAvailablePackages(@PathVariable Country country) {
        return packageService.getAvailablePackages(country);
    }

    // Get user's purchased packages
    @GetMapping("/user-purchased-package/{userId}")
    public List<UserPackageDto> getUserPackages(@PathVariable Long userId) {
        return packageService.getUserPackages(userId);
    }

    // Purchase a package
    @PostMapping("/purchase/{userId}/{packageId}")
    public UserPackageDto purchasePackage(@PathVariable Long userId, @PathVariable Long packageId) {
        return packageService.purchasePackage(userId, packageId);
    }
}

