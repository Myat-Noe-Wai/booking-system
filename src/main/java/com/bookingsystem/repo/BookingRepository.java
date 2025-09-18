package com.bookingsystem.repo;

import com.bookingsystem.application.enums.BookingStatus;
import com.bookingsystem.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    Optional<Booking> findByUserIdAndClassScheduleId(Long userId, Long classScheduleId);
    List<Booking> findByClassScheduleIdAndStatusOrderByBookedAtAsc(Long classScheduleId, BookingStatus status);
    List<Booking> findByClassScheduleIdAndStatus(Long classScheduleId, BookingStatus status);
    List<Booking> findByStatus(BookingStatus status);
}

