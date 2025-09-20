package com.bookingsystem.shared.config;

import com.bookingsystem.application.job.ExpiredPackageCleanupJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail expiredPackageJobDetail() {
        return JobBuilder.newJob(ExpiredPackageCleanupJob.class)
                .withIdentity("expiredPackageJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger expiredPackageTrigger(JobDetail expiredPackageJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(expiredPackageJobDetail)
                .withIdentity("expiredPackageTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 0)) // run daily at midnight
                .build();
    }
}

