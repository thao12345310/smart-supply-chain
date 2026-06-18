package com.distribution.service;

import com.distribution.model.Customer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC03 — Kiểm thử LUẬT hạn mức công nợ {@link Customer#hasAvailableCredit(java.math.BigDecimal)}.
 *
 * <p>Luật: chỉ cho phép phát sinh thêm công nợ khi (số dư hiện tại + số tiền) ≤ hạn mức.
 *
 * <p>GHI CHÚ TRUNG THỰC: luật đã hiện diện ở tầng domain và đúng đắn, NHƯNG hiện CHƯA được
 * gọi trong luồng duyệt đơn bán ({@code SalesOrderServiceImpl.approve()} mới chỉ kiểm tra
 * tồn khả dụng). Nối luật này vào việc chặn duyệt đơn là hạn chế còn lại / hướng phát triển.
 * Test kiểm thử đúng phạm vi đang có: tính đúng–sai của luật tại biên.
 */
class CreditLimitRuleTest {

    private Customer customer(String limit, String balance) {
        return Customer.builder()
            .code("C-" + System.nanoTime()).name("Credit Test Customer")
            .creditLimit(limit == null ? null : new BigDecimal(limit))
            .currentBalance(balance == null ? null : new BigDecimal(balance))
            .build();
    }

    @Test
    void allows_purchase_up_to_limit() {
        Customer c = customer("1000", "800");
        assertThat(c.hasAvailableCredit(new BigDecimal("200")))
            .as("800 + 200 = 1000 ≤ 1000 → cho phép").isTrue();
    }

    @Test
    void blocks_purchase_exceeding_limit() {
        Customer c = customer("1000", "800");
        assertThat(c.hasAvailableCredit(new BigDecimal("201")))
            .as("800 + 201 = 1001 > 1000 → chặn").isFalse();
    }

    @Test
    void treats_null_limit_and_balance_as_zero() {
        Customer c = customer(null, null);
        assertThat(c.hasAvailableCredit(BigDecimal.ZERO)).as("0 ≤ 0 → cho phép").isTrue();
        assertThat(c.hasAvailableCredit(new BigDecimal("1"))).as("1 > 0 → chặn").isFalse();
    }
}
