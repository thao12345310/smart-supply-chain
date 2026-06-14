package com.distribution.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: context khởi động được với profile test (H2), xác nhận toàn bộ bean
 * (gồm AccountingService hook vào GR/Invoice + dashboard) wire đúng, không vòng lặp dependency.
 */
@SpringBootTest
@ActiveProfiles("test")
class CoreFlowIT {

    @Test
    void application_context_loads_with_accounting_wired() {
        assertThat(true).isTrue();
    }
}
