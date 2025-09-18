package com.bookingsystem.model;

import com.bookingsystem.application.enums.Country;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "packages")
@Getter
@Setter
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String packageName; // e.g., "Basic Package SG"

    @Column(nullable = false)
    private int credits; // total credits provided

    @Column(nullable = false)
    private double price; // package price

    @Enumerated(EnumType.STRING)
    private Country country; // which country this package belongs to

    @Column(nullable = false)
    private LocalDate expiryDate; // default expiry date after purchase
}

