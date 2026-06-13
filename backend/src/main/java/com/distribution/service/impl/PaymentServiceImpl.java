package com.distribution.service.impl;

import com.distribution.dto.PaymentDTO;
import com.distribution.exception.BusinessException;
import com.distribution.model.Payment;
import com.distribution.model.enums.AccountCode;
import com.distribution.model.enums.PaymentType;
import com.distribution.repository.PaymentRepository;
import com.distribution.service.AccountingService;
import com.distribution.service.PaymentService;
import com.distribution.service.SalesInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountingService accountingService;
    private final SalesInvoiceService salesInvoiceService;

    @Override
    public PaymentDTO create(PaymentDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thanh toán phải > 0");
        }
        if (dto.getType() == null) {
            throw new BusinessException("Loại phiếu (thu/chi) là bắt buộc");
        }

        Payment payment = Payment.builder()
            .code(generateCode(dto.getType()))
            .type(dto.getType())
            .amount(dto.getAmount())
            .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now())
            .method(dto.getMethod())
            .salesInvoiceId(dto.getSalesInvoiceId())
            .purchaseOrderId(dto.getPurchaseOrderId())
            .note(dto.getNote())
            .build();
        payment = paymentRepository.save(payment);

        if (dto.getType() == PaymentType.RECEIPT) {
            accountingService.post(LocalDateTime.now(), "Phiếu thu " + payment.getCode(),
                "PAYMENT", payment.getId(), AccountCode.CASH, AccountCode.AR, payment.getAmount());
            if (dto.getSalesInvoiceId() != null) {
                salesInvoiceService.recordPayment(dto.getSalesInvoiceId(), dto.getAmount(),
                    dto.getMethod(), payment.getCode());
            }
        } else {
            accountingService.post(LocalDateTime.now(), "Phiếu chi " + payment.getCode(),
                "PAYMENT", payment.getId(), AccountCode.AP, AccountCode.CASH, payment.getAmount());
        }

        log.info("Tạo {} {} số tiền {}", payment.getType(), payment.getCode(), payment.getAmount());
        return toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAll() {
        return paymentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getByType(PaymentType type) {
        return paymentRepository.findByTypeOrderByIdDesc(type).stream().map(this::toDto).collect(Collectors.toList());
    }

    private String generateCode(PaymentType type) {
        return (type == PaymentType.RECEIPT ? "PT-" : "PC-") + System.currentTimeMillis();
    }

    private PaymentDTO toDto(Payment p) {
        return PaymentDTO.builder()
            .id(p.getId()).code(p.getCode()).type(p.getType()).amount(p.getAmount())
            .paymentDate(p.getPaymentDate()).method(p.getMethod())
            .salesInvoiceId(p.getSalesInvoiceId()).purchaseOrderId(p.getPurchaseOrderId())
            .note(p.getNote()).build();
    }
}
