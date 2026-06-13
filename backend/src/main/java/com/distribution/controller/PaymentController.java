package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import com.distribution.dto.PaymentDTO;
import com.distribution.model.enums.PaymentType;
import com.distribution.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> all(@RequestParam(required = false) PaymentType type) {
        List<PaymentDTO> data = (type != null) ? paymentService.getByType(type) : paymentService.getAll();
        return ResponseEntity.ok(ApiResponse.success(data, "Payments loaded"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDTO>> create(@RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.create(dto), "Payment created"));
    }
}
