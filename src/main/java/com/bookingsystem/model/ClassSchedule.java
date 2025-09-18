package com.bookingsystem.model;

import com.bookingsystem.application.enums.Country;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_schedule")
@Getter
@Setter
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className; // e.g. "Yoga 1hr"

    @Enumerated(EnumType.STRING)
    private Country country; // country the class belongs to

    private int requiredCredits; // credits needed to book this class

    private int totalSlots; // maximum participants

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @OneToMany(mappedBy = "classSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
}

