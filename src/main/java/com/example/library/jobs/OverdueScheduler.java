package com.example.library.jobs;

import com.example.library.domain.Issuance;
import com.example.library.service.IssuanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OverdueScheduler {
    private static final Logger log = LoggerFactory.getLogger(OverdueScheduler.class);
    private final IssuanceService issuanceService;

    public OverdueScheduler(IssuanceService issuanceService) {
        this.issuanceService = issuanceService;
    }

    // Run every day at 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void recomputeAndNotify() {
        issuanceService.recomputeFinesForAllActive();
        List<Issuance> overdue = issuanceService.listOverdue();
        for (Issuance i : overdue) {
            log.warn("Overdue: user={} book={} dueDate={} fine={}",
                    i.getUser().getUsername(),
                    i.getBook().getTitle(),
                    i.getDueDate(),
                    i.getFineAmount());
        }
    }
}


