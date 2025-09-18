package com.bookingsystem.model;

import com.bookingsystem.application.enums.PackageStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_packages")
@Getter
@Setter
public class UserPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private PackageEntity packageEntity;

    private int remainingCredits;

    private LocalDate expiryDate; // actual expiry date after purchase

    @Enumerated(EnumType.STRING)
    private PackageStatus packageStatus = PackageStatus.ACTIVE; // ACTIVE / EXPIRED
}

