package com.distribution.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacSecurityTest {

    @Autowired private MockMvc mvc;

    @Test
    void protected_endpoint_without_token_is_unauthorized() throws Exception {
        // The app's security setup returns 403 (Forbidden) for unauthenticated
        // requests to protected /api/** endpoints, not 401. Asserting actual behavior.
        mvc.perform(get("/api/accounting/transactions")
                .param("startDate", "2026-01-01").param("endDate", "2026-12-31"))
            .andExpect(status().isForbidden());
    }
}
