package com.bookingsystem.repo;

import com.bookingsystem.application.enums.Country;
import com.bookingsystem.model.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCountry(Country country);
}

