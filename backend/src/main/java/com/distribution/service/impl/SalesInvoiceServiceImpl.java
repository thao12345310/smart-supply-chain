package com.distribution.service.impl;

import com.distribution.dto.SalesInvoiceDTO;
import com.distribution.dto.SalesInvoiceItemDTO;
import com.distribution.exception.BusinessException;
import com.distribution.exception.ResourceNotFoundException;
import com.distribution.model.*;
import com.distribution.model.enums.SalesInvoiceStatus;
import com.distribution.model.enums.PaymentStatus;
import com.distribution.repository.*;
import com.distribution.service.SalesInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
public class SalesInvoiceServiceImpl implements SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceDTO getById(Long id) {
        SalesInvoice invoice = salesInvoiceRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice not found with ID: " + id));
        return mapToDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceDTO getByCode(String code) {
        SalesInvoice invoice = salesInvoiceRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice not found with code: " + code));
        return mapToDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getAll() {
        return salesInvoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getByStatus(SalesInvoiceStatus status) {
        return salesInvoiceRepository.findByStatus(status).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getBySalesOrderId(Long salesOrderId) {
        return salesInvoiceRepository.findBySalesOrderId(salesOrderId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceDTO getByGoodsIssueId(Long goodsIssueId) {
        SalesInvoice invoice = salesInvoiceRepository.findByGoodsIssueId(goodsIssueId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice not found for Goods Issue ID: " + goodsIssueId));
        return mapToDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getByCustomerId(Long customerId) {
        return salesInvoiceRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesInvoiceRepository.findByDateRange(startDate, endDate).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getOverdue() {
        return salesInvoiceRepository.findOverdue(LocalDate.now()).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getUnpaid() {
        return salesInvoiceRepository.findUnpaid().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> search(String query) {
        return salesInvoiceRepository.search(query).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public SalesInvoiceDTO issue(Long id, Long issuedBy) {
        log.info("Issuing invoice ID: {}", id);
        
        SalesInvoice invoice = salesInvoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice not found with ID: " + id));
        
        if (!invoice.getStatus().canIssue()) {
            throw new BusinessException("Invoice cannot be issued from status: " + invoice.getStatus());
        }
        
        invoice.setStatus(SalesInvoiceStatus.ISSUED);
        invoice.setIssuedDate(LocalDateTime.now());
        invoice.setIssuedBy(issuedBy);
        
        invoice = salesInvoiceRepository.save(invoice);
        log.info("Invoice {} issued", invoice.getCode());
        
        return mapToDTO(invoice);
    }

    @Override
    public SalesInvoiceDTO recordPayment(Long id, BigDecimal amount, String paymentMethod, String paymentReference) {
        log.info("Recording payment of {} for invoice ID: {}", amount, id);
        
        SalesInvoice invoice = salesInvoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice not found with ID: " + id));
        
        if (!invoice.getStatus().canReceivePayment()) {
            throw new BusinessException("Invoice cannot receive payment in status: " + invoice.getStatus());
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }
        
        if (amount.compareTo(invoice.getRemainingAmount()) > 0) {
            throw new BusinessException("Payment amount exceeds remaining amount");
        }
        
        // Update paid amount
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaymentReference(paymentReference);
        invoice.calculateRemainingAmount();
        
        // Update status
        if (invoice.isFullyPaid()) {
            invoice.setStatus(SalesInvoiceStatus.PAID);
            invoice.setPaidDate(LocalDateTime.now());
        } else {
            invoice.setStatus(SalesInvoiceStatus.PARTIALLY_PAID);
        }
        
        invoice = salesInvoiceRepository.save(invoice);
        
        // Update customer balance
        updateCustomerBalance(invoice.getCustomer().getId());
        
        // Update SO payment status
        updateSalesOrderPaymentStatus(invoice.getSalesOrder().getId());
        
        log.info("Payment recorded for invoice {}. New balance: {}", invoice.getCode(), invoice.getRemainingAmount());
        return mapToDTO(invoice);
    }

    @Override
    public SalesInvoiceDTO cancel(Long id) {
        log.info("Cancelling invoice ID: {}", id);
        
        SalesInvoice invoice = salesInvoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice not found with ID: " + id));
        
        if (!invoice.getStatus().canCancel()) {
            throw new BusinessException("Invoice cannot be cancelled from status: " + invoice.getStatus());
        }
        
        if (invoice.getPaidAmount() != null && invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot cancel invoice with existing payments");
        }
        
        invoice.setStatus(SalesInvoiceStatus.CANCELLED);
        invoice = salesInvoiceRepository.save(invoice);
        
        log.info("Invoice {} cancelled", invoice.getCode());
        return mapToDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstanding(Long customerId) {
        BigDecimal outstanding = salesInvoiceRepository.getTotalOutstandingByCustomerId(customerId);
        return outstanding != null ? outstanding : BigDecimal.ZERO;
    }

    // Helper methods

    private void updateCustomerBalance(Long customerId) {
        BigDecimal outstanding = getTotalOutstanding(customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setCurrentBalance(outstanding);
        customerRepository.save(customer);
    }

    private void updateSalesOrderPaymentStatus(Long salesOrderId) {
        List<SalesInvoice> invoices = salesInvoiceRepository.findBySalesOrderId(salesOrderId);
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));
        
        BigDecimal totalAmount = invoices.stream()
            .filter(inv -> inv.getStatus() != SalesInvoiceStatus.CANCELLED)
            .map(SalesInvoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = invoices.stream()
            .filter(inv -> inv.getStatus() != SalesInvoiceStatus.CANCELLED)
            .map(inv -> inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            salesOrder.setPaymentStatus(PaymentStatus.UNPAID);
        } else if (totalPaid.compareTo(totalAmount) >= 0) {
            salesOrder.setPaymentStatus(PaymentStatus.PAID);
        } else {
            salesOrder.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }
        
        salesOrderRepository.save(salesOrder);
    }

    private SalesInvoiceDTO mapToDTO(SalesInvoice invoice) {
        SalesInvoiceDTO dto = SalesInvoiceDTO.builder()
            .id(invoice.getId())
            .code(invoice.getCode())
            .status(invoice.getStatus())
            .invoiceDate(invoice.getInvoiceDate())
            .dueDate(invoice.getDueDate())
            .issuedDate(invoice.getIssuedDate())
            .paidDate(invoice.getPaidDate())
            .subtotal(invoice.getSubtotal())
            .taxAmount(invoice.getTaxAmount())
            .discountAmount(invoice.getDiscountAmount())
            .shippingCost(invoice.getShippingCost())
            .totalAmount(invoice.getTotalAmount())
            .paidAmount(invoice.getPaidAmount())
            .remainingAmount(invoice.getRemainingAmount())
            .paymentMethod(invoice.getPaymentMethod())
            .paymentReference(invoice.getPaymentReference())
            .notes(invoice.getNotes())
            .createdBy(invoice.getCreatedBy())
            .issuedBy(invoice.getIssuedBy())
            .createdAt(invoice.getCreatedAt())
            .build();
        
        // Sales Order info
        if (invoice.getSalesOrder() != null) {
            dto.setSalesOrderId(invoice.getSalesOrder().getId());
            dto.setSalesOrderCode(invoice.getSalesOrder().getCode());
        }
        
        // Goods Issue info
        if (invoice.getGoodsIssue() != null) {
            dto.setGoodsIssueId(invoice.getGoodsIssue().getId());
            dto.setGoodsIssueCode(invoice.getGoodsIssue().getCode());
        }
        
        // Customer info
        if (invoice.getCustomer() != null) {
            dto.setCustomerId(invoice.getCustomer().getId());
            dto.setCustomerName(invoice.getCustomer().getName());
            dto.setCustomerCode(invoice.getCustomer().getCode());
            dto.setCustomerEmail(invoice.getCustomer().getEmail());
        }
        
        // Items
        if (invoice.getItems() != null) {
            dto.setItems(invoice.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList()));
        }
        
        dto.computeFields();
        return dto;
    }

    private SalesInvoiceItemDTO mapItemToDTO(SalesInvoiceItem item) {
        return SalesInvoiceItemDTO.builder()
            .id(item.getId())
            .salesInvoiceId(item.getSalesInvoice().getId())
            .goodsIssueItemId(item.getGoodsIssueItem() != null ? item.getGoodsIssueItem().getId() : null)
            .productId(item.getProduct().getId())
            .productCode(item.getProduct().getCode())
            .productName(item.getProduct().getName())
            .description(item.getDescription())
            .quantity(item.getQuantity())
            .unit(item.getUnit())
            .unitPrice(item.getUnitPrice())
            .discountPercent(item.getDiscountPercent())
            .taxPercent(item.getTaxPercent())
            .amountBeforeTax(item.getAmountBeforeTax())
            .taxAmount(item.getTaxAmount())
            .totalAmount(item.getTotalAmount())
            .build();
    }
}
