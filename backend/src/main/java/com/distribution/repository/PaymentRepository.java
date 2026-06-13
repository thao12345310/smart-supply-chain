package com.distribution.repository;

import com.distribution.model.Payment;
import com.distribution.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTypeOrderByIdDesc(PaymentType type);
    List<Payment> findBySalesInvoiceId(Long salesInvoiceId);
    List<Payment> findByPaymentDateBetween(LocalDate from, LocalDate to);
}
