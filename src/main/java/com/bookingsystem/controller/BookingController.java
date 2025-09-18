package com.bookingsystem.controller;

import com.bookingsystem.application.dto.BookingRequestDto;
import com.bookingsystem.application.dto.BookingResponseDto;
import com.bookingsystem.application.dto.ClassScheduleDto;
import com.bookingsystem.application.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Book a class (will use the provided userPackageId to deduct credits).
     * userId is a required param here for simplicity; replace with authenticated principal in real app.
     */
    @PostMapping
    public ResponseEntity<BookingResponseDto> bookClass(@RequestParam Long userId,
                                                        @RequestBody BookingRequestDto dto) {
        BookingResponseDto resp = bookingService.bookClass(userId, dto);
        return ResponseEntity.ok(resp);
    }

    /**
     * Cancel booking
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId,
                                              @RequestParam Long userId) {
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check-in
     */
    @PostMapping("/{bookingId}/check-in")
    public ResponseEntity<Void> checkIn(@PathVariable Long bookingId,
                                        @RequestParam Long userId) {
        bookingService.checkIn(bookingId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * List all class schedules for a given country
     */
    @GetMapping("/schedules/{country}")
    public ResponseEntity<List<ClassScheduleDto>> getSchedulesByCountry(@PathVariable String country) {
        List<ClassScheduleDto> schedules = bookingService.getSchedulesByCountry(country);
        return ResponseEntity.ok(schedules);
    }
}

