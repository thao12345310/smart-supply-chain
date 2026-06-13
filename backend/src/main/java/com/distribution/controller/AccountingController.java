package com.distribution.controller;

import com.distribution.dto.AccountingDTO;
import com.distribution.dto.ApiResponse;
import com.distribution.model.enums.AccountCode;
import com.distribution.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN')")
public class AccountingController {

    private final AccountingService accountingService;

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<AccountingDTO.TransactionRow>>> transactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AccountingDTO.TransactionRow> rows = accountingService
            .getTransactions(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)).stream()
            .map(AccountingDTO.TransactionRow::of).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(rows, "Transactions loaded"));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<List<AccountingDTO.LedgerRow>>> ledger(@RequestParam AccountCode account) {
        List<AccountingDTO.LedgerRow> rows = accountingService.getLedger(account).stream()
            .map(AccountingDTO.LedgerRow::of).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(rows, "Ledger loaded"));
    }
}
