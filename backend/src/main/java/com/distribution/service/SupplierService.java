package com.distribution.service;

import com.distribution.dto.SupplierDTO;
import java.util.List;

public interface SupplierService {
    SupplierDTO create(SupplierDTO dto);
    SupplierDTO update(Long id, SupplierDTO dto);
    SupplierDTO getById(Long id);
    List<SupplierDTO> getAll();
    void delete(Long id);
}
