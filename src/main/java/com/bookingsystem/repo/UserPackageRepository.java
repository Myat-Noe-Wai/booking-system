package com.bookingsystem.repo;

import com.bookingsystem.model.User;
import com.bookingsystem.model.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    List<UserPackage> findByUser(User user);
    List<UserPackage> findByUserId(Long userId);
}

