package com.distribution.service;

import com.distribution.dto.PaymentDTO;
import com.distribution.model.enums.PaymentType;
import java.util.List;

public interface PaymentService {
    PaymentDTO create(PaymentDTO dto);
    List<PaymentDTO> getAll();
    List<PaymentDTO> getByType(PaymentType type);
}
