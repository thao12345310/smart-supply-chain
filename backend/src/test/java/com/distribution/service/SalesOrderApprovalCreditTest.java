package com.distribution.service;

import com.distribution.dto.SalesOrderDTO;
import com.distribution.exception.BusinessException;
import com.distribution.model.Customer;
import com.distribution.model.SalesOrder;
import com.distribution.model.enums.SalesOrderStatus;
import com.distribution.repository.CustomerRepository;
import com.distribution.repository.DeliveryAddressRepository;
import com.distribution.repository.ProductRepository;
import com.distribution.repository.SalesOrderItemRepository;
import com.distribution.repository.SalesOrderRepository;
import com.distribution.repository.WarehouseRepository;
import com.distribution.service.impl.SalesOrderServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TC04 — Kiểm thử LUẬT hạn mức công nợ ĐƯỢC ĐẤU vào luồng duyệt đơn bán
 * ({@link SalesOrderServiceImpl#approve(Long, Long)}).
 *
 * <p>Luật: chỉ duyệt đơn khi (công nợ hiện tại + giá trị đơn) ≤ hạn mức của khách.
 * Khác với {@code CreditLimitRuleTest} (kiểm thử thuần luật ở tầng domain), test này
 * xác nhận luật được THỰC THI tại bước duyệt: đơn vượt hạn mức bị từ chối và KHÔNG
 * giữ chỗ tồn kho.
 */
class SalesOrderApprovalCreditTest {

    private final SalesOrderRepository salesOrderRepository = mock(SalesOrderRepository.class);
    private final SalesOrderItemRepository salesOrderItemRepository = mock(SalesOrderItemRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final DeliveryAddressRepository deliveryAddressRepository = mock(DeliveryAddressRepository.class);
    private final WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final InventoryService inventoryService = mock(InventoryService.class);

    private final SalesOrderServiceImpl service = new SalesOrderServiceImpl(
        salesOrderRepository, salesOrderItemRepository, customerRepository,
        deliveryAddressRepository, warehouseRepository, productRepository, inventoryService);

    private SalesOrder orderFor(Customer customer, String grandTotal) {
        return SalesOrder.builder()
            .id(1L).code("SO-TEST-1")
            .status(SalesOrderStatus.ORDER_OPEN)
            .customer(customer)
            .grandTotal(new BigDecimal(grandTotal))
            .items(new HashSet<>())
            .build();
    }

    private Customer customer(String limit, String balance) {
        return Customer.builder()
            .code("C-CREDIT").name("Credit Customer")
            .creditLimit(new BigDecimal(limit))
            .currentBalance(new BigDecimal(balance))
            .build();
    }

    @Test
    void rejects_approval_when_order_exceeds_customer_credit_limit() {
        // available credit = 1000 - 800 = 200; order 300 -> 800 + 300 = 1100 > 1000
        SalesOrder so = orderFor(customer("1000", "800"), "300");
        when(salesOrderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(so));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.approve(1L, 99L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("credit");

        verify(inventoryService, never()).reserveInventory(anyLong(), anyLong(), anyInt());
    }

    @Test
    void allows_approval_when_order_within_credit_limit() {
        // available credit = 200; order 200 -> 800 + 200 = 1000 <= 1000 -> allowed
        SalesOrder so = orderFor(customer("1000", "800"), "200");
        when(salesOrderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(so));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        SalesOrderDTO result = service.approve(1L, 99L);

        assertThat(result.getStatus()).isEqualTo(SalesOrderStatus.ORDER_APPROVED);
        assertThat(so.getStatus()).isEqualTo(SalesOrderStatus.ORDER_APPROVED);
    }
}
