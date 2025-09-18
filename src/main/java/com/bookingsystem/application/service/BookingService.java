package com.bookingsystem.application.service;

import com.bookingsystem.application.dto.BookingRequestDto;
import com.bookingsystem.application.dto.BookingResponseDto;
import com.bookingsystem.application.dto.ClassScheduleDto;
import com.bookingsystem.application.enums.BookingStatus;
import com.bookingsystem.application.enums.Country;
import com.bookingsystem.model.Booking;
import com.bookingsystem.model.ClassSchedule;
import com.bookingsystem.model.UserPackage;
import com.bookingsystem.repo.BookingRepository;
import com.bookingsystem.repo.ClassScheduleRepository;
import com.bookingsystem.repo.UserPackageRepository;
import com.bookingsystem.shared.exception.GeneralException;
import com.bookingsystem.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ClassScheduleRepository classScheduleRepository;
    private final BookingRepository bookingRepository;
    private final UserPackageRepository userPackageRepository;
    private final RedisLockService redisLockService;

    // short TTL for lock to avoid long-held locks
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    /**
     * Concurrency-safe booking:
     * - Acquire per-class lock (redis)
     * - Re-check slot count under lock
     * - Deduct credits immediately for BOOKED or WAITLIST
     * - Save Booking with reference to used UserPackage
     */
    @Transactional
    public BookingResponseDto bookClass(Long userId, BookingRequestDto dto) {
        ClassSchedule schedule = classScheduleRepository.findById(dto.getClassScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", dto.getClassScheduleId()));

        UserPackage userPackage = userPackageRepository.findById(dto.getUserPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("UserPackage", dto.getUserPackageId()));

        // validate package ownership
        if (!userPackage.getUser().getId().equals(userId)) {
            throw new GeneralException("Package does not belong to user");
        }

        // package-country must match class-country
        if (!userPackage.getPackageEntity().getCountry().equals(schedule.getCountry())) {
            throw new GeneralException("Package country does not match class country");
        }

        // package expiry and credits
        if (userPackage.getExpiryDate().isBefore(LocalDate.now())) {
            throw new GeneralException("Package expired");
        }
        if (userPackage.getRemainingCredits() < schedule.getRequiredCredits()) {
            throw new GeneralException("Insufficient credits");
        }

        // check time overlap with user's existing BOOKED or CHECKED_IN classes
        List<Booking> userActiveBookings = bookingRepository.findByUserId(userId).stream()
                .filter(b -> b.getStatus() == BookingStatus.BOOKED || b.getStatus() == BookingStatus.CHECKED_IN)
                .collect(Collectors.toList());
        for (Booking b : userActiveBookings) {
            ClassSchedule s = b.getClassSchedule();
            if (timesOverlap(s.getStartTime(), s.getEndTime(), schedule.getStartTime(), schedule.getEndTime())) {
                throw new GeneralException("You have an overlapping booked class");
            }
        }

        // prevent duplicate (unless previous was canceled)
        Optional<Booking> existing = bookingRepository.findByUserIdAndClassScheduleId(userId, schedule.getId());
        if (existing.isPresent() && existing.get().getStatus() != BookingStatus.CANCELED) {
            throw new GeneralException("Already booked or waitlisted for this class");
        }

        String lockKey = "class_lock:" + schedule.getId();
        String token = redisLockService.tryLock(lockKey, LOCK_TTL);
        if (token == null) {
            // basic fail-fast; you can implement retries or exponential backoff if you want
            throw new GeneralException("Too many concurrent requests. Please try again.");
        }

        try {
            // recount current booked under lock
            long bookedCount = bookingRepository.findByClassScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED).size();

            Booking booking = new Booking();
            booking.setUser(userPackage.getUser());
            booking.setClassSchedule(schedule);
            booking.setUserPackage(userPackage);
            booking.setBookedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());

            if (bookedCount < schedule.getTotalSlots()) {
                booking.setStatus(BookingStatus.BOOKED);
            } else {
                booking.setStatus(BookingStatus.WAITLIST);
            }

            // deduct credits immediately for both BOOKED and WAITLIST per requirement
            userPackage.setRemainingCredits(userPackage.getRemainingCredits() - schedule.getRequiredCredits());
            userPackageRepository.save(userPackage);

            Booking saved = bookingRepository.save(booking);

            return new BookingResponseDto(
                    saved.getId(),
                    schedule.getId(),
                    schedule.getClassName(),
                    saved.getStatus(),
                    userPackage.getRemainingCredits()
            );

        } finally {
            redisLockService.releaseLock(lockKey, token);
        }
    }

    private boolean timesOverlap(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    /**
     * Cancel booking and handle refund & waitlist promotion.
     */
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new GeneralException("Not your booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELED) return;

        ClassSchedule schedule = booking.getClassSchedule();
        UserPackage usedPackage = booking.getUserPackage();

        // Refund rules:
        // - If was BOOKED and canceled more than 4 hours before class start => refund credits back to same UserPackage
        // - If was WAITLIST => refund immediately (credits were deducted at waitlist time)
        if (booking.getStatus() == BookingStatus.BOOKED) {
            if (schedule.getStartTime().isAfter(LocalDateTime.now().plusHours(4))) {
                // refund to same UserPackage used
                if (usedPackage != null) {
                    usedPackage.setRemainingCredits(usedPackage.getRemainingCredits() + schedule.getRequiredCredits());
                    userPackageRepository.save(usedPackage);
                }
            }
        } else if (booking.getStatus() == BookingStatus.WAITLIST) {
            if (usedPackage != null) {
                usedPackage.setRemainingCredits(usedPackage.getRemainingCredits() + schedule.getRequiredCredits());
                userPackageRepository.save(usedPackage);
            }
        }

        booking.setStatus(BookingStatus.CANCELED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Promote FIFO from waitlist -> booked under lock
        String lockKey = "class_lock:" + schedule.getId();
        String token = redisLockService.tryLock(lockKey, LOCK_TTL);
        if (token == null) {
            // can't promote now; scheduled job will handle promotion later.
            return;
        }

        try {
            long bookedCount = bookingRepository.findByClassScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED).size();
            if (bookedCount < schedule.getTotalSlots()) {
                List<Booking> waitlist = bookingRepository.findByClassScheduleIdAndStatusOrderByBookedAtAsc(schedule.getId(), BookingStatus.WAITLIST);
                if (!waitlist.isEmpty()) {
                    Booking first = waitlist.get(0);
                    first.setStatus(BookingStatus.BOOKED);
                    first.setUpdatedAt(LocalDateTime.now());
                    bookingRepository.save(first);
                    // credits for waitlist were deducted already when waitlisted, so no change needed.
                }
            }
        } finally {
            redisLockService.releaseLock(lockKey, token);
        }
    }

    /**
     * Check-in: allowed when class time reached and booking is BOOKED.
     */
    @Transactional
    public void checkIn(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new GeneralException("Not your booking");
        }

        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new GeneralException("Only booked users can check-in");
        }

        ClassSchedule schedule = booking.getClassSchedule();
        if (LocalDateTime.now().isBefore(schedule.getStartTime())) {
            throw new GeneralException("Cannot check-in before class start time");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    /**
     * Runs periodically to:
     * - refund WAITLIST bookings for classes that already ended (per requirement)
     * - try to promote waitlist to booked if slots freed but lock was previously unavailable
     *
     * Run every 5 minutes (adjust as you want).
     */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void scheduledProcessEndedClassesAndWaitlists() {
        LocalDateTime now = LocalDateTime.now();

        // 1) Refund waitlist entries for classes that ended
        List<Booking> waitlistsExpired = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.WAITLIST && b.getClassSchedule().getEndTime().isBefore(now))
                .collect(Collectors.toList());

        for (Booking b : waitlistsExpired) {
            UserPackage up = b.getUserPackage();
            if (up != null) {
                up.setRemainingCredits(up.getRemainingCredits() + b.getClassSchedule().getRequiredCredits());
                userPackageRepository.save(up);
            }
            b.setStatus(BookingStatus.CANCELED);
            b.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(b);
        }

        // 2) Try promoting waitlists for classes where booked < totalSlots
        List<ClassSchedule> schedules = classScheduleRepository.findAll();
        for (ClassSchedule schedule : schedules) {
            if (schedule.getEndTime().isBefore(now)) continue; // skip ended
            String lockKey = "class_lock:" + schedule.getId();
            String token = redisLockService.tryLock(lockKey, LOCK_TTL);
            if (token == null) continue; // skip this schedule now
            try {
                long bookedCount = bookingRepository.findByClassScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED).size();
                while (bookedCount < schedule.getTotalSlots()) {
                    List<Booking> waitlist = bookingRepository.findByClassScheduleIdAndStatusOrderByBookedAtAsc(schedule.getId(), BookingStatus.WAITLIST);
                    if (waitlist.isEmpty()) break;
                    Booking promote = waitlist.get(0);
                    promote.setStatus(BookingStatus.BOOKED);
                    promote.setUpdatedAt(LocalDateTime.now());
                    bookingRepository.save(promote);
                    bookedCount++;
                }
            } finally {
                redisLockService.releaseLock(lockKey, token);
            }
        }
    }

    /**
     * Get all class schedules by country
     */
    public List<ClassScheduleDto> getSchedulesByCountry(String countryStr) {
        // convert string to Country enum
        com.bookingsystem.application.enums.Country country;
        try {
            country = com.bookingsystem.application.enums.Country.valueOf(countryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException("Invalid country: " + countryStr);
        }

        List<ClassSchedule> schedules = classScheduleRepository.findByCountry(country);

        return schedules.stream()
                .map(s -> {
                    // calculate booked count
                    int bookedCount = bookingRepository
                            .findByClassScheduleIdAndStatus(s.getId(), BookingStatus.BOOKED)
                            .size();

                    return new ClassScheduleDto(
                            s.getId(),
                            s.getClassName(),
                            s.getCountry(),        // Country enum
                            s.getRequiredCredits(),
                            s.getTotalSlots(),
                            bookedCount,           // booked count
                            s.getStartTime(),
                            s.getEndTime()
                    );
                })
                .collect(Collectors.toList());
    }
}

