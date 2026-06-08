package com.distribution.scheduler;

import com.distribution.repository.SalesInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Scheduled job that flags unpaid sales invoices past their due date as OVERDUE.
 *
 * Invoices in status ISSUED or PARTIALLY_PAID whose dueDate is before today are
 * bulk-updated to OVERDUE. The cron expression defaults to 01:00 every day and can
 * be overridden via the {@code app.overdue-check.cron} property.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueInvoiceScheduler {

    private final SalesInvoiceRepository salesInvoiceRepository;

    @Scheduled(cron = "${app.overdue-check.cron:0 0 1 * * *}")
    @Transactional
    public void markOverdueInvoices() {
        LocalDate today = LocalDate.now();
        int updated = salesInvoiceRepository.markOverdue(today);
        if (updated > 0) {
            log.info("Overdue check: marked {} invoice(s) as OVERDUE (due before {})", updated, today);
        } else {
            log.debug("Overdue check: no invoices to mark as OVERDUE (due before {})", today);
        }
    }
}
