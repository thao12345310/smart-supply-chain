package com.distribution.service;

import com.distribution.dto.DeliveryOrderDTO;
import com.distribution.model.DeliveryOrder;
import com.distribution.model.GoodsIssue;
import com.distribution.repository.DeliveryOrderRepository;
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

    /**
     * All vận đơn available to be added to a delivery plan (derived from confirmed phiếu xuất).
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
                            .build()));
            result.add(toDTO(order, gi));
        }
        return result;
    }

    /**
     * Vận đơn already linked to a given plan.
     */
    @Transactional
    public List<DeliveryOrderDTO> listByPlan(Long planId) {
        return deliveryOrderRepository.findByDeliveryPlanId(planId).stream()
                .map(o -> toDTO(o, goodsIssueRepository.findByCode(o.getCode()).orElse(null)))
                .collect(Collectors.toList());
    }

    private DeliveryOrderDTO toDTO(DeliveryOrder order, GoodsIssue gi) {
        return DeliveryOrderDTO.builder()
                .id(order.getId())
                .code(order.getCode())
                .status(order.getStatus())
                .customerName(customerName(gi))
                .deliveryAddress(gi != null && formatAddress(gi) != null
                        ? formatAddress(gi) : order.getDestinationAddress())
                .build();
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
