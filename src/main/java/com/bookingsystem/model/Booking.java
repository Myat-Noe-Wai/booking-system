package com.bookingsystem.model;

import com.bookingsystem.application.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private ClassSchedule classSchedule;

    /**
     * The package the user used for this booking.
     * We store it so refunds are applied exactly to the right UserPackage.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private UserPackage userPackage;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.BOOKED;
    // BOOKED, WAITLIST, CANCELED, CHECKED_IN

    private LocalDateTime bookedAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}

