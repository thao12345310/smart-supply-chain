package com.distribution.service;

import com.distribution.dto.WarehouseDTO;
import java.util.List;

public interface WarehouseService {
    WarehouseDTO create(WarehouseDTO dto);
    WarehouseDTO update(Long id, WarehouseDTO dto);
    WarehouseDTO getById(Long id);
    List<WarehouseDTO> getAll();
    void delete(Long id);
}
