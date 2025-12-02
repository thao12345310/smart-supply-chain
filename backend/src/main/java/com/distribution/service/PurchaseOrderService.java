package com.distribution.service;

import com.distribution.dto.PurchaseOrderDTO;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderDTO create(PurchaseOrderDTO dto);
    PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto);
    PurchaseOrderDTO getById(Long id);
    List<PurchaseOrderDTO> getAll();
    void delete(Long id);
}
