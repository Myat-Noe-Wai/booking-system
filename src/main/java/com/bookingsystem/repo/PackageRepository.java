package com.bookingsystem.repo;

import com.bookingsystem.model.PackageEntity;
import com.bookingsystem.application.enums.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageRepository extends JpaRepository<PackageEntity, Long> {
    List<PackageEntity> findByCountry(Country country);
}

