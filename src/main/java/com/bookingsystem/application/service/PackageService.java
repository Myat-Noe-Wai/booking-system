package com.bookingsystem.application.service;

import com.bookingsystem.application.dto.PackageDto;
import com.bookingsystem.application.dto.UserPackageDto;
import com.bookingsystem.application.enums.Country;
import com.bookingsystem.application.enums.PackageStatus;
import com.bookingsystem.model.PackageEntity;
import com.bookingsystem.model.User;
import com.bookingsystem.model.UserPackage;
import com.bookingsystem.repo.PackageRepository;
import com.bookingsystem.repo.UserPackageRepository;
import com.bookingsystem.repo.UserRepository;
import com.bookingsystem.shared.exception.PaymentFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageService {
    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserPackageRepository userPackageRepository;

    @Autowired
    private UserRepository userRepository;

    public List<PackageDto> getAvailablePackages(Country country) {
        return packageRepository.findByCountry(country)
                .stream()
                .map(pkg -> {
                    PackageDto dto = new PackageDto();
                    dto.setId(pkg.getId());
                    dto.setPackageName(pkg.getPackageName());
                    dto.setCredits(pkg.getCredits());
                    dto.setPrice(pkg.getPrice());
                    dto.setCountry(pkg.getCountry());
                    dto.setExpiryDate(pkg.getExpiryDate());
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<UserPackageDto> getUserPackages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return userPackageRepository.findByUser(user)
                .stream()
                .map(up -> {
                    UserPackageDto dto = new UserPackageDto();
                    dto.setId(up.getId());
                    dto.setPackageName(up.getPackageEntity().getPackageName());
                    dto.setRemainingCredits(up.getRemainingCredits());
                    dto.setExpiryDate(up.getExpiryDate());
                    dto.setPackageStatus(up.getExpiryDate().isBefore(LocalDate.now()) ? PackageStatus.EXPIRED : PackageStatus.ACTIVE);
                    return dto;
                }).collect(Collectors.toList());
    }

    public UserPackageDto purchasePackage(Long userId, Long packageId) {
        User user = userRepository.findById(userId).orElseThrow();
        PackageEntity pkg = packageRepository.findById(packageId).orElseThrow();

        // Step 1: Add payment card (mock)
        boolean cardAdded = addPaymentCard(user.getId(), "mock-card-token");
        if (!cardAdded) {
            throw new PaymentFailedException("Failed to add payment card!");
        }

        // Step 2: Charge payment (mock)
        boolean paymentSuccess = paymentCharge(user.getId(), pkg.getPrice());
        if (!paymentSuccess) {
            throw new PaymentFailedException("Payment failed!");
        }

        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setPackageEntity(pkg);
        userPackage.setRemainingCredits(pkg.getCredits());
        userPackage.setExpiryDate(pkg.getExpiryDate());
        userPackage.setPackageStatus(PackageStatus.ACTIVE);

        userPackageRepository.save(userPackage);

        UserPackageDto dto = new UserPackageDto();
        dto.setId(userPackage.getId());
        dto.setPackageName(pkg.getPackageName());
        dto.setRemainingCredits(pkg.getCredits());
        dto.setExpiryDate(pkg.getExpiryDate());
        dto.setPackageStatus(PackageStatus.ACTIVE);
        return dto;
    }

    // ---------------- MOCK FUNCTIONS ----------------
    private boolean addPaymentCard(Long userId, String cardToken) {
        // Mock implementation: Always succeeds
        return true;
    }

    private boolean paymentCharge(Long userId, Double amount) {
        // Mock implementation: Always succeeds
        return true;
    }
}
