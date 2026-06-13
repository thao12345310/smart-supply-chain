package com.distribution.service.impl;

import com.distribution.dto.DashboardDTO;
import com.distribution.model.*;
import com.distribution.model.enums.*;
import com.distribution.repository.*;
import com.distribution.service.AccountingService;
import com.distribution.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DashboardService (Phân hệ 5 – Báo cáo & Dashboard)
 *
 * Provides overview statistics, revenue charts,
 * inventory stock reports, and receivables reports.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLotRepository inventoryLotRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final DeliveryTripRouteRepository deliveryTripRouteRepository;
    private final PaymentRepository paymentRepository;
    private final AccountingService accountingService;

    // ==================== Overview Summary ====================

    @Override
    public DashboardDTO.OverviewSummary getOverviewSummary() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstDayLastMonth = firstDayThisMonth.minusMonths(1);
        LocalDate lastDayLastMonth = firstDayThisMonth.minusDays(1);

        // Revenue calculation from paid/partially-paid invoices
        BigDecimal revenueThisMonth = calculateRevenue(firstDayThisMonth, today);
        BigDecimal revenueLastMonth = calculateRevenue(firstDayLastMonth, lastDayLastMonth);
        BigDecimal totalRevenue = calculateTotalRevenue();

        // Growth percent
        Double growthPercent = null;
        if (revenueLastMonth != null && revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            growthPercent = revenueThisMonth.subtract(revenueLastMonth)
                    .divide(revenueLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // Order counts
        long totalSO = salesOrderRepository.count();
        long pendingSO = salesOrderRepository.countByStatus(SalesOrderStatus.ORDER_OPEN);
        long totalPO = purchaseOrderRepository.count();
        long pendingPO = purchaseOrderRepository.countByStatus(PurchaseOrderStatus.ORDER_OPEN);

        // Inventory counts (khả dụng đã trừ hàng hết hạn chờ hủy)
        List<Inventory> allInventory = inventoryRepository.findAll();
        Map<String, Integer> expiredMap = buildExpiredQuantityMap();
        long totalProducts = productRepository.count();
        long lowStockItems = allInventory.stream()
                .map(i -> effectiveAvailable(i, expiredMap))
                .filter(a -> a > 0 && a <= 10)
                .count();
        long outOfStockItems = allInventory.stream()
                .filter(i -> effectiveAvailable(i, expiredMap) <= 0)
                .count();
        BigDecimal totalInventoryValue = allInventory.stream()
                .filter(i -> i.getAverageCost() != null && i.getQuantityOnHand() != null)
                .map(i -> i.getAverageCost().multiply(BigDecimal.valueOf(i.getQuantityOnHand())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Receivables
        List<SalesInvoice> unpaidInvoices = salesInvoiceRepository.findUnpaid();
        BigDecimal totalReceivables = unpaidInvoices.stream()
                .map(si -> si.getRemainingAmount() != null ? si.getRemainingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesInvoice> overdueInvoices = salesInvoiceRepository.findOverdue(today);
        BigDecimal totalOverdueReceivables = overdueInvoices.stream()
                .map(si -> si.getRemainingAmount() != null ? si.getRemainingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.OverviewSummary.builder()
                .totalRevenue(totalRevenue)
                .totalRevenueThisMonth(revenueThisMonth)
                .totalRevenueLastMonth(revenueLastMonth)
                .revenueGrowthPercent(growthPercent)
                .totalSalesOrders(totalSO)
                .pendingSalesOrders(pendingSO)
                .totalPurchaseOrders(totalPO)
                .pendingPurchaseOrders(pendingPO)
                .totalProducts(totalProducts)
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .totalInventoryValue(totalInventoryValue)
                .totalReceivables(totalReceivables)
                .totalOverdueReceivables(totalOverdueReceivables)
                .overdueInvoiceCount((long) overdueInvoices.size())
                .pendingDeliveries(0L)
                .completedDeliveriesToday(0L)
                .build();
    }

    // ==================== Revenue Chart ====================

    @Override
    public DashboardDTO.RevenueChartData getRevenueChart(LocalDate startDate, LocalDate endDate, String groupBy) {
        List<SalesInvoice> invoices = salesInvoiceRepository.findByDateRange(startDate, endDate);

        Map<String, List<SalesInvoice>> grouped;
        DateTimeFormatter formatter;

        if ("monthly".equalsIgnoreCase(groupBy)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            grouped = invoices.stream()
                    .collect(Collectors.groupingBy(
                            si -> si.getInvoiceDate().format(formatter),
                            TreeMap::new,
                            Collectors.toList()
                    ));
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            grouped = invoices.stream()
                    .collect(Collectors.groupingBy(
                            si -> si.getInvoiceDate().format(formatter),
                            TreeMap::new,
                            Collectors.toList()
                    ));
        }

        List<DashboardDTO.RevenuePoint> dataPoints = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Map.Entry<String, List<SalesInvoice>> entry : grouped.entrySet()) {
            BigDecimal revenue = entry.getValue().stream()
                    .filter(si -> si.getTotalAmount() != null)
                    .map(SalesInvoice::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dataPoints.add(DashboardDTO.RevenuePoint.builder()
                    .label(entry.getKey())
                    .revenue(revenue)
                    .cost(BigDecimal.ZERO) // Can be extended with purchase costs
                    .orderCount((long) entry.getValue().size())
                    .build());

            totalRevenue = totalRevenue.add(revenue);
        }

        return DashboardDTO.RevenueChartData.builder()
                .data(dataPoints)
                .total(totalRevenue)
                .period(groupBy)
                .build();
    }

    // ==================== Inventory Report (Nhập-Xuất-Tồn) ====================

    @Override
    public DashboardDTO.InventoryReportSummary getInventoryReport(
            LocalDate startDate, LocalDate endDate, Long warehouseId) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get transactions within the period
        List<InventoryTransaction> transactions = inventoryTransactionRepository
                .findByTransactionDateBetweenOrderByTransactionDateDesc(startDateTime, endDateTime);

        // Get current inventory state
        List<Inventory> currentInventory;
        if (warehouseId != null) {
            currentInventory = inventoryRepository.findByWarehouseId(warehouseId);
            transactions = transactions.stream()
                    .filter(t -> t.getWarehouse() != null && warehouseId.equals(t.getWarehouse().getId()))
                    .collect(Collectors.toList());
        } else {
            currentInventory = inventoryRepository.findAll();
        }

        // Group transactions by product
        Map<Long, List<InventoryTransaction>> txByProduct = transactions.stream()
                .filter(t -> t.getProduct() != null)
                .collect(Collectors.groupingBy(t -> t.getProduct().getId()));

        // Group current inventory by product
        Map<Long, List<Inventory>> invByProduct = currentInventory.stream()
                .filter(i -> i.getProduct() != null)
                .collect(Collectors.groupingBy(i -> i.getProduct().getId()));

        // Tồn hết hạn theo (product, warehouse) để tính khả dụng thực
        Map<String, Integer> expiredMap = buildExpiredQuantityMap();

        // Build report items
        // Merge product IDs from both sources
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(txByProduct.keySet());
        allProductIds.addAll(invByProduct.keySet());

        List<DashboardDTO.InventoryReportDTO> items = new ArrayList<>();
        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        int totalQuantityOnHand = 0;
        long lowStockCount = 0;
        long outOfStockCount = 0;

        for (Long productId : allProductIds) {
            List<InventoryTransaction> productTx = txByProduct.getOrDefault(productId, Collections.emptyList());
            List<Inventory> productInv = invByProduct.getOrDefault(productId, Collections.emptyList());

            // Product info from first available source
            Product product = null;
            if (!productInv.isEmpty()) {
                product = productInv.get(0).getProduct();
            } else if (!productTx.isEmpty()) {
                product = productTx.get(0).getProduct();
            }
            if (product == null) continue;

            // Calculate totals from transactions
            int totalReceived = productTx.stream()
                    .filter(t -> t.getTransactionType().getMultiplier() > 0)
                    .mapToInt(t -> t.getQuantity() != null ? t.getQuantity() : 0)
                    .sum();

            int totalIssued = productTx.stream()
                    .filter(t -> t.getTransactionType().getMultiplier() < 0)
                    .mapToInt(t -> t.getQuantity() != null ? t.getQuantity() : 0)
                    .sum();

            // Current closing stock from inventory
            int closingStock = productInv.stream()
                    .mapToInt(i -> i.getQuantityOnHand() != null ? i.getQuantityOnHand() : 0)
                    .sum();

            // Opening = Closing - Received + Issued
            int openingStock = closingStock - totalReceived + totalIssued;

            // Average cost
            BigDecimal avgCost = productInv.stream()
                    .filter(i -> i.getAverageCost() != null)
                    .map(Inventory::getAverageCost)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal closingValue = avgCost.multiply(BigDecimal.valueOf(closingStock));

            // Warehouse breakdown
            List<DashboardDTO.WarehouseStock> warehouseBreakdown = productInv.stream()
                    .map(inv -> DashboardDTO.WarehouseStock.builder()
                            .warehouseId(inv.getWarehouse() != null ? inv.getWarehouse().getId() : null)
                            .warehouseName(inv.getWarehouse() != null ? inv.getWarehouse().getName() : "N/A")
                            .quantity(inv.getQuantityOnHand() != null ? inv.getQuantityOnHand() : 0)
                            .reserved(inv.getQuantityReserved() != null ? inv.getQuantityReserved() : 0)
                            .available(effectiveAvailable(inv, expiredMap))
                            .build())
                    .collect(Collectors.toList());

            items.add(DashboardDTO.InventoryReportDTO.builder()
                    .productId(product.getId())
                    .productCode(product.getCode())
                    .productName(product.getName())
                    .category(null) // Product entity does not have category field
                    .unit(null)     // Product entity does not have unit field
                    .openingStock(openingStock)
                    .totalReceived(totalReceived)
                    .totalIssued(totalIssued)
                    .adjustments(0)
                    .closingStock(closingStock)
                    .averageCost(avgCost)
                    .closingValue(closingValue)
                    .warehouseBreakdown(warehouseBreakdown)
                    .build());

            totalInventoryValue = totalInventoryValue.add(closingValue);
            totalQuantityOnHand += closingStock;
            if (closingStock <= 0) outOfStockCount++;
            else if (closingStock <= 10) lowStockCount++;
        }

        // Sort by product name
        items.sort(Comparator.comparing(DashboardDTO.InventoryReportDTO::getProductName, 
                Comparator.nullsLast(Comparator.naturalOrder())));

        return DashboardDTO.InventoryReportSummary.builder()
                .totalInventoryValue(totalInventoryValue)
                .totalProducts((long) items.size())
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .totalQuantityOnHand(totalQuantityOnHand)
                .items(items)
                .build();
    }

    // ==================== Receivables Report (Công nợ) ====================

    @Override
    public DashboardDTO.ReceivablesReportSummary getReceivablesReport(boolean overdueOnly) {
        LocalDate today = LocalDate.now();

        List<Customer> customers = customerRepository.findAll();
        List<SalesInvoice> allUnpaid = salesInvoiceRepository.findUnpaid();

        // Group invoices by customer
        Map<Long, List<SalesInvoice>> invoicesByCustomer = allUnpaid.stream()
                .filter(si -> si.getCustomer() != null)
                .collect(Collectors.groupingBy(si -> si.getCustomer().getId()));

        // If overdueOnly, also filter overdue invoices
        List<SalesInvoice> overdueInvoices = salesInvoiceRepository.findOverdue(today);
        Set<Long> customerIdsWithOverdue = overdueInvoices.stream()
                .filter(si -> si.getCustomer() != null)
                .map(si -> si.getCustomer().getId())
                .collect(Collectors.toSet());

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        long totalOverdueInvoiceCount = 0;
        List<DashboardDTO.ReceivableDTO> result = new ArrayList<>();

        for (Customer customer : customers) {
            List<SalesInvoice> customerInvoices = invoicesByCustomer.getOrDefault(customer.getId(), Collections.emptyList());
            if (customerInvoices.isEmpty()) continue;
            if (overdueOnly && !customerIdsWithOverdue.contains(customer.getId())) continue;

            BigDecimal custTotal = BigDecimal.ZERO;
            BigDecimal custPaid = BigDecimal.ZERO;
            BigDecimal custOutstanding = BigDecimal.ZERO;
            long overdueCount = 0;
            int maxOverdueDays = 0;

            List<DashboardDTO.InvoiceSummary> invoiceSummaries = new ArrayList<>();

            for (SalesInvoice invoice : customerInvoices) {
                BigDecimal invTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal invPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal invRemaining = invoice.getRemainingAmount() != null ? invoice.getRemainingAmount() : invTotal.subtract(invPaid);

                custTotal = custTotal.add(invTotal);
                custPaid = custPaid.add(invPaid);
                custOutstanding = custOutstanding.add(invRemaining);

                int overdueDays = 0;
                if (invoice.getDueDate() != null && today.isAfter(invoice.getDueDate())) {
                    overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), today);
                    overdueCount++;
                    maxOverdueDays = Math.max(maxOverdueDays, overdueDays);
                }

                invoiceSummaries.add(DashboardDTO.InvoiceSummary.builder()
                        .invoiceId(invoice.getId())
                        .invoiceCode(invoice.getCode())
                        .salesOrderCode(invoice.getSalesOrder() != null ? invoice.getSalesOrder().getCode() : null)
                        .invoiceDate(invoice.getInvoiceDate())
                        .dueDate(invoice.getDueDate())
                        .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                        .totalAmount(invTotal)
                        .paidAmount(invPaid)
                        .remainingAmount(invRemaining)
                        .overdueDays(overdueDays)
                        .build());
            }

            result.add(DashboardDTO.ReceivableDTO.builder()
                    .customerId(customer.getId())
                    .customerCode(customer.getCode())
                    .customerName(customer.getName())
                    .phone(customer.getPhone())
                    .totalInvoiceAmount(custTotal)
                    .totalPaidAmount(custPaid)
                    .outstandingAmount(custOutstanding)
                    .totalInvoices((long) customerInvoices.size())
                    .overdueInvoices(overdueCount)
                    .maxOverdueDays(maxOverdueDays)
                    .invoices(invoiceSummaries)
                    .build());

            totalOutstanding = totalOutstanding.add(custOutstanding);
            totalOverdue = totalOverdue.add(
                    invoiceSummaries.stream()
                            .filter(s -> s.getOverdueDays() > 0)
                            .map(DashboardDTO.InvoiceSummary::getRemainingAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
            totalOverdueInvoiceCount += overdueCount;
        }

        // Sort by outstanding amount desc
        result.sort(Comparator.comparing(DashboardDTO.ReceivableDTO::getOutstandingAmount,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return DashboardDTO.ReceivablesReportSummary.builder()
                .totalOutstanding(totalOutstanding)
                .totalOverdue(totalOverdue)
                .totalCustomersWithDebt((long) result.size())
                .totalOverdueInvoices(totalOverdueInvoiceCount)
                .customers(result)
                .build();
    }

    // ==================== Top Selling Products ====================

    @Override
    public List<DashboardDTO.TopProductDTO> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        List<SalesInvoice> invoices = salesInvoiceRepository.findByDateRange(startDate, endDate);

        // Aggregate quantities and revenue per product
        Map<Long, int[]> productQuantities = new HashMap<>(); // productId -> [totalQty]
        Map<Long, BigDecimal> productRevenues = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();

        for (SalesInvoice invoice : invoices) {
            if (invoice.getItems() == null) continue;
            for (SalesInvoiceItem item : invoice.getItems()) {
                if (item.getProduct() == null) continue;
                Long pid = item.getProduct().getId();
                productMap.putIfAbsent(pid, item.getProduct());

                productQuantities.computeIfAbsent(pid, k -> new int[]{0})[0] += 
                        (item.getQuantity() != null ? item.getQuantity() : 0);
                productRevenues.merge(pid,
                        item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO,
                        BigDecimal::add);
            }
        }

        return productRevenues.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Product p = productMap.get(entry.getKey());
                    return DashboardDTO.TopProductDTO.builder()
                            .productId(entry.getKey())
                            .productCode(p != null ? p.getCode() : null)
                            .productName(p != null ? p.getName() : null)
                            .totalQuantitySold(productQuantities.getOrDefault(entry.getKey(), new int[]{0})[0])
                            .totalRevenue(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== Per-Cluster Dashboards ====================

    @Override
    public DashboardDTO.PurchaseDashboard getPurchaseDashboard() {
        List<PurchaseOrder> all = purchaseOrderRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate firstDayThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        long totalPO = all.size();
        long pendingApproval = all.stream()
                .filter(po -> po.getStatus() == PurchaseOrderStatus.ORDER_OPEN)
                .count();
        long pendingReceipt = all.stream()
                .filter(po -> po.getStatus() == PurchaseOrderStatus.ORDER_APPROVED
                        || po.getStatus() == PurchaseOrderStatus.ORDER_PARTIALLY_RECEIVED)
                .count();

        BigDecimal purchaseValueThisMonth = all.stream()
                .filter(po -> po.getCreatedDate() != null
                        && !po.getCreatedDate().isBefore(firstDayThisMonth)
                        && !po.getCreatedDate().isAfter(today))
                .map(po -> po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardDTO.ClusterChartPoint> poByStatus = all.stream()
                .filter(po -> po.getStatus() != null)
                .collect(Collectors.groupingBy(po -> po.getStatus().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey())
                        .value(BigDecimal.valueOf(e.getValue()))
                        .build())
                .collect(Collectors.toList());

        List<DashboardDTO.ClusterChartPoint> topSuppliers = all.stream()
                .filter(po -> po.getSupplier() != null)
                .collect(Collectors.groupingBy(
                        po -> po.getSupplier().getName() != null ? po.getSupplier().getName() : "N/A",
                        Collectors.reducing(BigDecimal.ZERO,
                                po -> po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO,
                                BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        return DashboardDTO.PurchaseDashboard.builder()
                .totalPO(totalPO)
                .pendingApproval(pendingApproval)
                .pendingReceipt(pendingReceipt)
                .purchaseValueThisMonth(purchaseValueThisMonth)
                .poByStatus(poByStatus)
                .topSuppliers(topSuppliers)
                .build();
    }

    @Override
    public DashboardDTO.SalesDashboard getSalesDashboard() {
        List<SalesOrder> all = salesOrderRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate firstDayThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        long totalSO = all.size();

        // Revenue this month from issued sales invoices (excludes draft/cancelled)
        BigDecimal revenueThisMonth = salesInvoiceRepository.findByDateRange(firstDayThisMonth, today).stream()
                .filter(si -> si.getTotalAmount() != null)
                .map(SalesInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardDTO.ClusterChartPoint> soByStatus = all.stream()
                .filter(so -> so.getStatus() != null)
                .collect(Collectors.groupingBy(so -> so.getStatus().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey())
                        .value(BigDecimal.valueOf(e.getValue()))
                        .build())
                .collect(Collectors.toList());

        List<DashboardDTO.ClusterChartPoint> topCustomers = all.stream()
                .filter(so -> so.getCustomer() != null)
                .collect(Collectors.groupingBy(
                        so -> so.getCustomer().getName() != null ? so.getCustomer().getName() : "N/A",
                        Collectors.reducing(BigDecimal.ZERO,
                                so -> so.getTotalAmount() != null ? so.getTotalAmount() : BigDecimal.ZERO,
                                BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        return DashboardDTO.SalesDashboard.builder()
                .totalSO(totalSO)
                .revenueThisMonth(revenueThisMonth)
                .soByStatus(soByStatus)
                .topCustomers(topCustomers)
                .build();
    }

    @Override
    public DashboardDTO.InventoryDashboard getInventoryDashboard() {
        List<Inventory> all = inventoryRepository.findAll();
        Map<String, Integer> expiredMap = buildExpiredQuantityMap();

        BigDecimal totalStockValue = all.stream()
                .filter(i -> i.getAverageCost() != null && i.getQuantityOnHand() != null)
                .map(i -> i.getAverageCost().multiply(BigDecimal.valueOf(i.getQuantityOnHand())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = all.stream()
                .filter(i -> {
                    int avail = effectiveAvailable(i, expiredMap);
                    if (i.getReorderLevel() != null) {
                        return avail <= i.getReorderLevel();
                    }
                    return avail > 0 && avail <= 10;
                })
                .count();

        LocalDate threshold = LocalDate.now().plusDays(30);
        long expiringSoonCount = inventoryLotRepository.findExpiringSoon(threshold).size();
        long expiredCount = inventoryLotRepository.findExpired().size();

        List<DashboardDTO.ClusterChartPoint> stockByWarehouse = all.stream()
                .filter(i -> i.getWarehouse() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getWarehouse().getName() != null ? i.getWarehouse().getName() : "N/A",
                        Collectors.reducing(BigDecimal.ZERO,
                                i -> {
                                    if (i.getAverageCost() != null && i.getQuantityOnHand() != null) {
                                        return i.getAverageCost().multiply(BigDecimal.valueOf(i.getQuantityOnHand()));
                                    }
                                    return BigDecimal.ZERO;
                                },
                                BigDecimal::add)))
                .entrySet().stream()
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());

        return DashboardDTO.InventoryDashboard.builder()
                .totalStockValue(totalStockValue)
                .lowStockCount(lowStockCount)
                .expiringSoonCount(expiringSoonCount)
                .expiredCount(expiredCount)
                .stockByWarehouse(stockByWarehouse)
                .build();
    }

    @Override
    public DashboardDTO.DeliveryDashboard getDeliveryDashboard() {
        List<DeliveryTripRoute> all = deliveryTripRouteRepository.findAll();

        long totalTrips = all.size();
        long completedTrips = all.stream()
                .filter(t -> t.getStatus() == DeliveryTripRoute.TripStatus.COMPLETED)
                .count();
        double successRate = totalTrips > 0
                ? (completedTrips * 100.0) / totalTrips
                : 0.0;

        List<DashboardDTO.ClusterChartPoint> tripsByStatus = all.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey())
                        .value(BigDecimal.valueOf(e.getValue()))
                        .build())
                .collect(Collectors.toList());

        // Trips grouped by shipper (shipperName legacy field, fall back to shipperUser username)
        List<DashboardDTO.ClusterChartPoint> ordersByShipper = all.stream()
                .collect(Collectors.groupingBy(this::resolveShipperName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> DashboardDTO.ClusterChartPoint.builder()
                        .label(e.getKey())
                        .value(BigDecimal.valueOf(e.getValue()))
                        .build())
                .collect(Collectors.toList());

        return DashboardDTO.DeliveryDashboard.builder()
                .totalTrips(totalTrips)
                .completedTrips(completedTrips)
                .successRate(successRate)
                .tripsByStatus(tripsByStatus)
                .ordersByShipper(ordersByShipper)
                .build();
    }

    @Override
    public DashboardDTO.AccountingDashboard getAccountingDashboard() {
        BigDecimal totalReceivable = lastRunningBalance(AccountCode.AR);
        BigDecimal totalPayable = lastRunningBalance(AccountCode.AP).abs();

        BigDecimal cashIn = paymentRepository.findByTypeOrderByIdDesc(PaymentType.RECEIPT).stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashOut = paymentRepository.findByTypeOrderByIdDesc(PaymentType.DISBURSEMENT).stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueInvoices = salesInvoiceRepository.findOverdue(LocalDate.now()).size();

        return DashboardDTO.AccountingDashboard.builder()
                .totalReceivable(totalReceivable)
                .totalPayable(totalPayable)
                .cashIn(cashIn)
                .cashOut(cashOut)
                .overdueInvoices(overdueInvoices)
                .cashFlowByMonth(java.util.List.of())
                .build();
    }

    // ==================== Private Helpers ====================

    private String resolveShipperName(DeliveryTripRoute trip) {
        if (trip.getShipperName() != null && !trip.getShipperName().isBlank()) {
            return trip.getShipperName();
        }
        if (trip.getShipperUser() != null && trip.getShipperUser().getUsername() != null) {
            return trip.getShipperUser().getUsername();
        }
        return "Unassigned";
    }

    private BigDecimal lastRunningBalance(AccountCode account) {
        List<AccountingService.LedgerLine> ledger = accountingService.getLedger(account);
        if (ledger == null || ledger.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal bal = ledger.get(ledger.size() - 1).runningBalance();
        return bal != null ? bal : BigDecimal.ZERO;
    }


    // Map "productId:warehouseId" -> tổng tồn của các lô đã hết HSD
    private Map<String, Integer> buildExpiredQuantityMap() {
        return inventoryLotRepository.sumExpiredQuantityGrouped().stream()
                .collect(Collectors.toMap(
                        row -> row[0] + ":" + row[1],
                        row -> ((BigDecimal) row[2]).intValue()
                ));
    }

    // Khả dụng thực = khả dụng - tồn hết hạn chờ hủy (không âm)
    private int effectiveAvailable(Inventory inv, Map<String, Integer> expiredMap) {
        int available = inv.getQuantityAvailable() != null ? inv.getQuantityAvailable() : 0;
        String key = (inv.getProduct() != null ? inv.getProduct().getId() : null)
                + ":" + (inv.getWarehouse() != null ? inv.getWarehouse().getId() : null);
        return Math.max(0, available - expiredMap.getOrDefault(key, 0));
    }

    private BigDecimal calculateRevenue(LocalDate startDate, LocalDate endDate) {
        List<SalesInvoice> invoices = salesInvoiceRepository.findByDateRange(startDate, endDate);
        return invoices.stream()
                .filter(si -> si.getTotalAmount() != null)
                .map(SalesInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalRevenue() {
        List<SalesInvoice> allInvoices = salesInvoiceRepository.findAll();
        return allInvoices.stream()
                .filter(si -> si.getStatus() != SalesInvoiceStatus.CANCELLED 
                        && si.getStatus() != SalesInvoiceStatus.DRAFT)
                .filter(si -> si.getTotalAmount() != null)
                .map(SalesInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
