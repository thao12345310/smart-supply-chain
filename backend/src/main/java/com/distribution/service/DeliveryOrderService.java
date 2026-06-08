package com.distribution.service;

import com.distribution.dto.DeliveryOrderDTO;
import com.distribution.model.DeliveryOrder;
import com.distribution.model.GoodsIssue;
import com.distribution.repository.DeliveryOrderRepository;
import com.distribution.repository.DeliveryPlanOrderRepository;
import com.distribution.repository.DeliveryTripRouteItemRepository;
import com.distribution.repository.GoodsIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the list of "vận đơn" (delivery orders) from confirmed Goods Issues (phiếu xuất).
 *
 * A DeliveryOrder row is materialized once per confirmed GoodsIssue (keyed by GI code) so that
 * it can be linked into a delivery plan via DeliveryPlanOrder.
 */
@Service
@RequiredArgsConstructor
public class DeliveryOrderService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final GoodsIssueRepository goodsIssueRepository;
    private final DeliveryPlanOrderRepository deliveryPlanOrderRepository;
    private final DeliveryTripRouteItemRepository tripItemRepository;

    /**
     * Vận đơn còn khả dụng để thêm vào một đợt giao hàng: sinh từ các phiếu xuất kho
     * đã xác nhận (hàng đã thực sự ra khỏi kho = "đơn đã bán được") và CHƯA được gom
     * vào bất kỳ đợt giao nào — tránh việc chọn lại đơn đã giao/đã phân chuyến.
     */
    @Transactional
    public List<DeliveryOrderDTO> listAvailable() {
        List<DeliveryOrderDTO> result = new ArrayList<>();
        for (GoodsIssue gi : goodsIssueRepository.findConfirmed()) {
            DeliveryOrder order = deliveryOrderRepository.findByCode(gi.getCode())
                    .orElseGet(() -> deliveryOrderRepository.save(DeliveryOrder.builder()
                            .code(gi.getCode())
                            .status("Pending")
                            .destinationAddress(formatAddress(gi))
                            .salesOrder(gi.getSalesOrder())
                            .build()));
            // Gán liên kết đơn bán cho các vận đơn được tạo trước khi có liên kết này
            if (order.getSalesOrder() == null && gi.getSalesOrder() != null) {
                order.setSalesOrder(gi.getSalesOrder());
                deliveryOrderRepository.save(order);
            }
            // Lọc: bỏ qua vận đơn đã thuộc một đợt giao hàng
            if (deliveryPlanOrderRepository.existsByDeliveryOrderId(order.getId())) {
                continue;
            }
            result.add(toDTO(order, gi));
        }
        return result;
    }

    /**
     * Vận đơn already linked to a given plan.
     */
    @Transactional
    public List<DeliveryOrderDTO> listByPlan(Long planId) {
        List<Long> assignedOrderIds = tripItemRepository.findAssignedOrderIdsByPlanId(planId);
        return deliveryOrderRepository.findByDeliveryPlanId(planId).stream()
                .map(o -> {
                    DeliveryOrderDTO dto = toDTO(o, goodsIssueRepository.findByCode(o.getCode()).orElse(null));
                    dto.setAssignedToTrip(assignedOrderIds.contains(o.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private DeliveryOrderDTO toDTO(DeliveryOrder order, GoodsIssue gi) {
        return DeliveryOrderDTO.builder()
                .id(order.getId())
                .code(order.getCode())
                .salesOrderCode(salesOrderCode(order, gi))
                .status(order.getStatus())
                .customerName(customerName(gi))
                .deliveryAddress(gi != null && formatAddress(gi) != null
                        ? formatAddress(gi) : order.getDestinationAddress())
                .build();
    }

    private String salesOrderCode(DeliveryOrder order, GoodsIssue gi) {
        if (gi != null && gi.getSalesOrder() != null) {
            return gi.getSalesOrder().getCode();
        }
        if (order.getSalesOrder() != null) {
            return order.getSalesOrder().getCode();
        }
        return null;
    }

    private String customerName(GoodsIssue gi) {
        if (gi != null && gi.getSalesOrder() != null && gi.getSalesOrder().getCustomer() != null) {
            return gi.getSalesOrder().getCustomer().getName();
        }
        return null;
    }

    private String formatAddress(GoodsIssue gi) {
        if (gi != null && gi.getDeliveryAddress() != null) {
            return gi.getDeliveryAddress().getFullAddress();
        }
        return null;
    }
}
