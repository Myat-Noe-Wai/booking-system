package com.bookingsystem.application.job;

import com.bookingsystem.model.UserPackage;
import com.bookingsystem.repo.UserPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredPackageCleanupJob implements Job {

    private final UserPackageRepository userPackageRepository;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Quartz job started: Cleaning up expired user packages");

        List<UserPackage> expired = userPackageRepository.findAll().stream()
                .filter(up -> up.getExpiryDate().isBefore(LocalDate.now()))
                .toList();

        for (UserPackage up : expired) {
            // Example: set credits to 0 for expired packages
            up.setRemainingCredits(0);
            userPackageRepository.save(up);
        }

        log.info("Quartz job completed: {} expired packages processed", expired.size());
    }
}
