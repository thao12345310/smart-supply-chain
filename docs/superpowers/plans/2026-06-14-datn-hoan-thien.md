# Hoàn thiện DATN Quản lý phân phối — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bổ sung 5 hạng mục còn thiếu của đồ án — Kế toán tối thiểu, Vận đơn (in được), Dashboard từng cụm, Đo response time, và bộ Test đại diện — để khớp đầy đủ Phiếu giao nhiệm vụ.

**Architecture:** Spring Boot phân tầng controller → service(+impl) → repository → entity (JPA). Kế toán móc vào các điểm xác nhận nghiệp vụ có sẵn (`GoodsReceiptServiceImpl.confirm`, `SalesInvoiceServiceImpl.issue`, Payment) trong cùng transaction. Frontend React + Ant Design + Recharts, mỗi trang gọi qua `services/api.js`, response đã bóc `ApiResponse.data` ở interceptor.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, Spring Security (JWT/RBAC, `@EnableMethodSecurity`), Flyway (migration V9+), PostgreSQL, Micrometer/Actuator; React 18 + Vite + Ant Design 5 + Recharts + axios.

---

## ⚠️ Ràng buộc bắt buộc đọc trước khi code

1. **Flyway BẬT + `ddl-auto: validate`** (xem `backend/src/main/resources/application.yml`). Mọi entity/cột mới **phải** có file migration `V{n}__*.sql` (migration cuối hiện tại là `V8`). Không có migration → app fail khi khởi động vì Hibernate `validate` không khớp schema.
2. **Response API bọc trong `ApiResponse`** (`{success, message, data}`). Controller mới nên trả `ApiResponse.success(payload, msg)`. Frontend interceptor (`services/api.js`) đã tự bóc `.data` khi thấy field `success` — nên ở frontend chỉ cần đọc `res.data`.
3. **Convention DTO**: dùng nested static class trong 1 DTO theo nhóm (xem `DashboardDTO`), hoặc DTO riêng kèm `@Builder`. Map thủ công trong service (không dùng MapStruct).
4. **RBAC**: role `ACCOUNTANT` đã có trong `RoleType`. Bảo vệ endpoint kế toán bằng `@PreAuthorize("hasAnyRole('ACCOUNTANT','ADMIN')")` (method security đã bật).
5. **Tiền tệ dùng `BigDecimal`** (theo `SalesInvoice`).
6. **Chạy backend test:** `cd backend && ./mvnw test` (hoặc `mvn test`). Hiện chưa có thư mục `backend/src/test` — Phase 5 tạo mới.

---

## File Structure (tổng quan thay đổi)

**Backend mới:**
- `model/Account.java`, `model/AccountingTransaction.java`, `model/Payment.java` + enums `AccountCode`, `PaymentType`
- `repository/AccountRepository.java`, `AccountingTransactionRepository.java`, `PaymentRepository.java`
- `service/AccountingService.java` (+`impl/AccountingServiceImpl.java`), `service/PaymentService.java` (+impl)
- `controller/AccountingController.java`, `PaymentController.java`
- `dto/AccountingDTO.java`, `PaymentDTO.java`
- `config/RequestTimingInterceptor.java`, `config/WebMvcConfig.java`, `controller/MetricsController.java`
- `resources/db/migration/V9__accounting_module.sql`, `V10__delivery_order_fields.sql`
- `backend/src/test/java/com/distribution/...` (Phase 5)

**Backend sửa:**
- `service/impl/GoodsReceiptServiceImpl.java` (hook AP), `service/impl/SalesInvoiceServiceImpl.java` (hook AR/Revenue)
- `model/DeliveryOrder.java` (+ field người nhận/ngày), `service/DeliveryOrderService.java`, `controller/DeliveryOrderController.java` (thêm detail + filter)
- `service/DashboardService.java` + `service/impl/DashboardServiceImpl.java` + `controller/DashboardController.java` (5 endpoint cụm)
- `resources/application.yml` (expose actuator metrics)

**Frontend mới:**
- `pages/DeliveryOrderList.jsx`, `pages/DeliveryOrderDetail.jsx`, `pages/WaybillPrint.jsx`
- `pages/PaymentList.jsx`, `pages/LedgerPage.jsx`
- `pages/dashboards/PurchaseDashboard.jsx`, `SalesDashboard.jsx`, `InventoryDashboard.jsx`, `DeliveryDashboard.jsx`, `AccountingDashboard.jsx`

**Frontend sửa:**
- `services/api.js` (thêm `paymentApi`, `accountingApi`, `deliveryOrderApi`, mở rộng `dashboardApi`)
- `App.jsx` (import + routes + menu)

**Scripts mới:**
- `scripts/perf/run.sh` (load test), `scripts/perf/README.md`

---

# PHASE 1 — Module Kế toán (tối thiểu)

Làm trước vì rủi ro cao nhất (đụng GR/Invoice). Sau mỗi task chạy `mvn -q compile` để bắt lỗi sớm.

### Task 1.1: Enum AccountCode & PaymentType

**Files:**
- Create: `backend/src/main/java/com/distribution/model/enums/AccountCode.java`
- Create: `backend/src/main/java/com/distribution/model/enums/PaymentType.java`

- [ ] **Step 1: Tạo `AccountCode.java`**

```java
package com.distribution.model.enums;

/** Tập tài khoản cố định, tối giản cho module kế toán (không cho người dùng tạo thêm). */
public enum AccountCode {
    CASH("Tiền mặt/Ngân hàng"),
    AR("Phải thu khách hàng"),
    AP("Phải trả nhà cung cấp"),
    REVENUE("Doanh thu"),
    INVENTORY("Hàng tồn kho"),
    EXPENSE("Chi phí / Giá vốn");

    private final String displayName;
    AccountCode(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
```

- [ ] **Step 2: Tạo `PaymentType.java`**

```java
package com.distribution.model.enums;

/** RECEIPT = phiếu thu (khách trả tiền); DISBURSEMENT = phiếu chi (trả NCC). */
public enum PaymentType {
    RECEIPT("Phiếu thu"),
    DISBURSEMENT("Phiếu chi");

    private final String displayName;
    PaymentType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
```

- [ ] **Step 3: Compile**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS (chưa dùng enum ở đâu nên chỉ kiểm tra cú pháp)

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/distribution/model/enums/AccountCode.java backend/src/main/java/com/distribution/model/enums/PaymentType.java
git commit -m "feat(accounting): add AccountCode and PaymentType enums"
```

---

### Task 1.2: Entity Account, AccountingTransaction, Payment

**Files:**
- Create: `backend/src/main/java/com/distribution/model/Account.java`
- Create: `backend/src/main/java/com/distribution/model/AccountingTransaction.java`
- Create: `backend/src/main/java/com/distribution/model/Payment.java`

- [ ] **Step 1: Tạo `Account.java`**

```java
package com.distribution.model;

import com.distribution.model.enums.AccountCode;
import jakarta.persistence.*;
import lombok.*;

/** Danh mục tài khoản kế toán (seed sẵn 6 tài khoản qua migration). */
@Entity
@Table(name = "account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private AccountCode code;

    @Column(nullable = false)
    private String name;
}
```

- [ ] **Step 2: Tạo `AccountingTransaction.java`**

```java
package com.distribution.model;

import com.distribution.model.enums.AccountCode;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bút toán đơn giản: mỗi bản ghi = 1 cặp Nợ/Có (debit/credit) cho 1 số tiền.
 * Sinh tự động từ nghiệp vụ, không cho sửa tay.
 */
@Entity
@Table(name = "accounting_transaction", indexes = {
    @Index(name = "idx_acctx_date", columnList = "tx_date"),
    @Index(name = "idx_acctx_source", columnList = "source_type, source_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tx_date", nullable = false)
    private LocalDateTime txDate;

    @Column(nullable = false, length = 255)
    private String description;

    /** GR | INVOICE | PAYMENT */
    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "debit_account", nullable = false, length = 20)
    private AccountCode debitAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_account", nullable = false, length = 20)
    private AccountCode creditAccount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
}
```

- [ ] **Step 3: Tạo `Payment.java`**

```java
package com.distribution.model;

import com.distribution.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Phiếu thu/chi. RECEIPT gắn salesInvoiceId; DISBURSEMENT gắn purchaseOrderId. */
@Entity
@Table(name = "payment", indexes = {
    @Index(name = "idx_payment_type", columnList = "type"),
    @Index(name = "idx_payment_date", columnList = "payment_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(length = 50)
    private String method;

    @Column(name = "sales_invoice_id")
    private Long salesInvoiceId;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(length = 500)
    private String note;
}
```

- [ ] **Step 4: Compile**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/distribution/model/Account.java backend/src/main/java/com/distribution/model/AccountingTransaction.java backend/src/main/java/com/distribution/model/Payment.java
git commit -m "feat(accounting): add Account, AccountingTransaction, Payment entities"
```

---

### Task 1.3: Migration V9 — bảng kế toán + seed account

**Files:**
- Create: `backend/src/main/resources/db/migration/V9__accounting_module.sql`

- [ ] **Step 1: Viết migration**

```sql
-- V9: Module kế toán tối thiểu — account, accounting_transaction, payment

CREATE TABLE account (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

INSERT INTO account (code, name) VALUES
    ('CASH',      'Tiền mặt/Ngân hàng'),
    ('AR',        'Phải thu khách hàng'),
    ('AP',        'Phải trả nhà cung cấp'),
    ('REVENUE',   'Doanh thu'),
    ('INVENTORY', 'Hàng tồn kho'),
    ('EXPENSE',   'Chi phí / Giá vốn');

CREATE TABLE accounting_transaction (
    id             BIGSERIAL PRIMARY KEY,
    tx_date        TIMESTAMP NOT NULL,
    description    VARCHAR(255) NOT NULL,
    source_type    VARCHAR(20),
    source_id      BIGINT,
    debit_account  VARCHAR(20) NOT NULL,
    credit_account VARCHAR(20) NOT NULL,
    amount         NUMERIC(18,2) NOT NULL
);
CREATE INDEX idx_acctx_date   ON accounting_transaction(tx_date);
CREATE INDEX idx_acctx_source ON accounting_transaction(source_type, source_id);

CREATE TABLE payment (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(255) NOT NULL UNIQUE,
    type              VARCHAR(20) NOT NULL,
    amount            NUMERIC(18,2) NOT NULL,
    payment_date      DATE NOT NULL,
    method            VARCHAR(50),
    sales_invoice_id  BIGINT,
    purchase_order_id BIGINT,
    note              VARCHAR(500)
);
CREATE INDEX idx_payment_type ON payment(type);
CREATE INDEX idx_payment_date ON payment(payment_date);

COMMENT ON TABLE accounting_transaction IS 'Bút toán Nợ/Có đơn giản, sinh tự động từ GR/Invoice/Payment';
COMMENT ON TABLE payment IS 'Phiếu thu (RECEIPT) / phiếu chi (DISBURSEMENT)';
```

- [ ] **Step 2: Chạy app để Flyway apply + Hibernate validate**

Run: `cd backend && mvn -q spring-boot:run` (Ctrl+C sau khi thấy "Started")
Expected: log Flyway "Migrating schema ... to version 9" và app khởi động không lỗi `SchemaManagementException`. Nếu lỗi validate → đối chiếu kiểu cột với entity (Task 1.2).

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/V9__accounting_module.sql
git commit -m "feat(accounting): V9 migration for account, accounting_transaction, payment"
```

---

### Task 1.4: Repositories

**Files:**
- Create: `backend/src/main/java/com/distribution/repository/AccountRepository.java`
- Create: `backend/src/main/java/com/distribution/repository/AccountingTransactionRepository.java`
- Create: `backend/src/main/java/com/distribution/repository/PaymentRepository.java`

- [ ] **Step 1: `AccountRepository.java`**

```java
package com.distribution.repository;

import com.distribution.model.Account;
import com.distribution.model.enums.AccountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(AccountCode code);
}
```

- [ ] **Step 2: `AccountingTransactionRepository.java`**

```java
package com.distribution.repository;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountingTransactionRepository extends JpaRepository<AccountingTransaction, Long> {

    List<AccountingTransaction> findByTxDateBetweenOrderByTxDateDesc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT t FROM AccountingTransaction t " +
           "WHERE t.debitAccount = :acc OR t.creditAccount = :acc ORDER BY t.txDate ASC")
    List<AccountingTransaction> findByAccount(@Param("acc") AccountCode acc);
}
```

- [ ] **Step 3: `PaymentRepository.java`**

```java
package com.distribution.repository;

import com.distribution.model.Payment;
import com.distribution.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTypeOrderByIdDesc(PaymentType type);
    List<Payment> findBySalesInvoiceId(Long salesInvoiceId);
    List<Payment> findByPaymentDateBetween(LocalDate from, LocalDate to);
}
```

- [ ] **Step 4: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/repository/AccountRepository.java backend/src/main/java/com/distribution/repository/AccountingTransactionRepository.java backend/src/main/java/com/distribution/repository/PaymentRepository.java
git commit -m "feat(accounting): add repositories"
```

---

### Task 1.5: AccountingService (post bút toán)

**Files:**
- Create: `backend/src/main/java/com/distribution/service/AccountingService.java`
- Create: `backend/src/main/java/com/distribution/service/impl/AccountingServiceImpl.java`

- [ ] **Step 1: Interface `AccountingService.java`**

```java
package com.distribution.service;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountingService {

    /** Ghi 1 bút toán Nợ/Có. Gọi từ các service nghiệp vụ trong cùng transaction. */
    AccountingTransaction post(LocalDateTime when, String description,
                              String sourceType, Long sourceId,
                              AccountCode debit, AccountCode credit, BigDecimal amount);

    List<AccountingTransaction> getTransactions(LocalDateTime from, LocalDateTime to);

    /** Sổ cái 1 tài khoản kèm số dư lũy kế (Nợ +, Có -). */
    List<LedgerLine> getLedger(AccountCode account);

    record LedgerLine(AccountingTransaction tx, BigDecimal debit, BigDecimal credit, BigDecimal runningBalance) {}
}
```

- [ ] **Step 2: Impl `AccountingServiceImpl.java`**

```java
package com.distribution.service.impl;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import com.distribution.repository.AccountingTransactionRepository;
import com.distribution.service.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountingServiceImpl implements AccountingService {

    private final AccountingTransactionRepository txRepo;

    @Override
    public AccountingTransaction post(LocalDateTime when, String description,
                                      String sourceType, Long sourceId,
                                      AccountCode debit, AccountCode credit, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Bỏ qua bút toán {} vì amount không hợp lệ: {}", description, amount);
            return null;
        }
        AccountingTransaction tx = AccountingTransaction.builder()
            .txDate(when != null ? when : LocalDateTime.now())
            .description(description)
            .sourceType(sourceType)
            .sourceId(sourceId)
            .debitAccount(debit)
            .creditAccount(credit)
            .amount(amount)
            .build();
        tx = txRepo.save(tx);
        log.info("Bút toán #{}: Nợ {} / Có {} = {} ({})", tx.getId(), debit, credit, amount, description);
        return tx;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingTransaction> getTransactions(LocalDateTime from, LocalDateTime to) {
        return txRepo.findByTxDateBetweenOrderByTxDateDesc(from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerLine> getLedger(AccountCode account) {
        List<AccountingTransaction> txs = txRepo.findByAccount(account);
        List<LedgerLine> lines = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;
        for (AccountingTransaction tx : txs) {
            BigDecimal debit = tx.getDebitAccount() == account ? tx.getAmount() : BigDecimal.ZERO;
            BigDecimal credit = tx.getCreditAccount() == account ? tx.getAmount() : BigDecimal.ZERO;
            balance = balance.add(debit).subtract(credit);
            lines.add(new LedgerLine(tx, debit, credit, balance));
        }
        return lines;
    }
}
```

- [ ] **Step 3: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/service/AccountingService.java backend/src/main/java/com/distribution/service/impl/AccountingServiceImpl.java
git commit -m "feat(accounting): AccountingService post + ledger"
```

---

### Task 1.6: Hook bút toán vào GR (Nợ INVENTORY / Có AP)

**Files:**
- Modify: `backend/src/main/java/com/distribution/service/impl/GoodsReceiptServiceImpl.java`

- [ ] **Step 1: Thêm dependency AccountingService**

Sau dòng `private final InventoryLotRepository inventoryLotRepo;` (dòng ~55), thêm:

```java
    private final com.distribution.service.AccountingService accountingService;
```

- [ ] **Step 2: Post bút toán cuối hàm `confirm(...)`**

Trong `confirm(...)`, ngay **trước** `return toDto(saved);` (dòng ~368), thêm:

```java
        // Bút toán nhập kho: Nợ Hàng tồn / Có Phải trả NCC (giá trị hàng nhận)
        if (saved.getTotalAmount() != null) {
            accountingService.post(
                java.time.LocalDateTime.now(),
                "Nhập kho " + saved.getCode(),
                "GR", saved.getId(),
                com.distribution.model.enums.AccountCode.INVENTORY,
                com.distribution.model.enums.AccountCode.AP,
                saved.getTotalAmount()
            );
        }
```

- [ ] **Step 3: Compile**

Run: `cd backend && mvn -q compile`
Expected: BUILD SUCCESS (constructor `@RequiredArgsConstructor` tự thêm tham số mới)

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/distribution/service/impl/GoodsReceiptServiceImpl.java
git commit -m "feat(accounting): auto-post journal entry on goods receipt confirm"
```

---

### Task 1.7: Hook bút toán vào Invoice issue (Nợ AR / Có REVENUE)

**Files:**
- Modify: `backend/src/main/java/com/distribution/service/impl/SalesInvoiceServiceImpl.java`

- [ ] **Step 1: Thêm dependency**

Sau `private final SalesOrderRepository salesOrderRepository;` (dòng ~32), thêm:

```java
    private final com.distribution.service.AccountingService accountingService;
```

- [ ] **Step 2: Post bút toán trong `issue(...)`**

Trong `issue(...)`, ngay **trước** `return mapToDTO(invoice);` (dòng ~140), thêm:

```java
        // Bút toán ghi nhận doanh thu: Nợ Phải thu KH / Có Doanh thu
        if (invoice.getTotalAmount() != null) {
            accountingService.post(
                java.time.LocalDateTime.now(),
                "Hóa đơn bán hàng " + invoice.getCode(),
                "INVOICE", invoice.getId(),
                com.distribution.model.enums.AccountCode.AR,
                com.distribution.model.enums.AccountCode.REVENUE,
                invoice.getTotalAmount()
            );
        }
```

- [ ] **Step 3: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/service/impl/SalesInvoiceServiceImpl.java
git commit -m "feat(accounting): auto-post revenue journal entry on invoice issue"
```

---

### Task 1.8: PaymentService + DTO (thu/chi → bút toán + cập nhật trạng thái HĐ)

**Files:**
- Create: `backend/src/main/java/com/distribution/dto/PaymentDTO.java`
- Create: `backend/src/main/java/com/distribution/service/PaymentService.java`
- Create: `backend/src/main/java/com/distribution/service/impl/PaymentServiceImpl.java`

- [ ] **Step 1: `PaymentDTO.java`**

```java
package com.distribution.dto;

import com.distribution.model.enums.PaymentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDTO {
    private Long id;
    private String code;
    private PaymentType type;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private Long salesInvoiceId;
    private Long purchaseOrderId;
    private String note;
}
```

- [ ] **Step 2: `PaymentService.java`**

```java
package com.distribution.service;

import com.distribution.dto.PaymentDTO;
import com.distribution.model.enums.PaymentType;
import java.util.List;

public interface PaymentService {
    PaymentDTO create(PaymentDTO dto);
    List<PaymentDTO> getAll();
    List<PaymentDTO> getByType(PaymentType type);
}
```

- [ ] **Step 3: `PaymentServiceImpl.java`**

```java
package com.distribution.service.impl;

import com.distribution.dto.PaymentDTO;
import com.distribution.exception.BusinessException;
import com.distribution.model.Payment;
import com.distribution.model.SalesInvoice;
import com.distribution.model.enums.AccountCode;
import com.distribution.model.enums.PaymentType;
import com.distribution.repository.PaymentRepository;
import com.distribution.service.AccountingService;
import com.distribution.service.PaymentService;
import com.distribution.service.SalesInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountingService accountingService;
    private final SalesInvoiceService salesInvoiceService;

    @Override
    public PaymentDTO create(PaymentDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thanh toán phải > 0");
        }
        if (dto.getType() == null) {
            throw new BusinessException("Loại phiếu (thu/chi) là bắt buộc");
        }

        Payment payment = Payment.builder()
            .code(generateCode(dto.getType()))
            .type(dto.getType())
            .amount(dto.getAmount())
            .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now())
            .method(dto.getMethod())
            .salesInvoiceId(dto.getSalesInvoiceId())
            .purchaseOrderId(dto.getPurchaseOrderId())
            .note(dto.getNote())
            .build();
        payment = paymentRepository.save(payment);

        if (dto.getType() == PaymentType.RECEIPT) {
            // Thu KH: Nợ Tiền / Có Phải thu — và ghi nhận vào hóa đơn để cập nhật PaymentStatus
            accountingService.post(LocalDateTime.now(), "Phiếu thu " + payment.getCode(),
                "PAYMENT", payment.getId(), AccountCode.CASH, AccountCode.AR, payment.getAmount());
            if (dto.getSalesInvoiceId() != null) {
                salesInvoiceService.recordPayment(dto.getSalesInvoiceId(), dto.getAmount(),
                    dto.getMethod(), payment.getCode());
            }
        } else {
            // Chi NCC: Nợ Phải trả / Có Tiền
            accountingService.post(LocalDateTime.now(), "Phiếu chi " + payment.getCode(),
                "PAYMENT", payment.getId(), AccountCode.AP, AccountCode.CASH, payment.getAmount());
        }

        log.info("Tạo {} {} số tiền {}", payment.getType(), payment.getCode(), payment.getAmount());
        return toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAll() {
        return paymentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getByType(PaymentType type) {
        return paymentRepository.findByTypeOrderByIdDesc(type).stream().map(this::toDto).collect(Collectors.toList());
    }

    private String generateCode(PaymentType type) {
        return (type == PaymentType.RECEIPT ? "PT-" : "PC-") + System.currentTimeMillis();
    }

    private PaymentDTO toDto(Payment p) {
        return PaymentDTO.builder()
            .id(p.getId()).code(p.getCode()).type(p.getType()).amount(p.getAmount())
            .paymentDate(p.getPaymentDate()).method(p.getMethod())
            .salesInvoiceId(p.getSalesInvoiceId()).purchaseOrderId(p.getPurchaseOrderId())
            .note(p.getNote()).build();
    }
}
```

> Lưu ý: `salesInvoiceService.recordPayment(...)` đã tồn tại (xem `SalesInvoiceServiceImpl`) và sẽ cập nhật `PaymentStatus`. Vì payment service và invoice service chạy trong cùng transaction, nếu `recordPayment` ném lỗi (vd vượt số còn nợ) thì rollback cả phiếu thu.

- [ ] **Step 4: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/dto/PaymentDTO.java backend/src/main/java/com/distribution/service/PaymentService.java backend/src/main/java/com/distribution/service/impl/PaymentServiceImpl.java
git commit -m "feat(accounting): PaymentService creates payment + journal + updates invoice status"
```

---

### Task 1.9: Controllers (Payment + Accounting) + DTO map

**Files:**
- Create: `backend/src/main/java/com/distribution/dto/AccountingDTO.java`
- Create: `backend/src/main/java/com/distribution/controller/PaymentController.java`
- Create: `backend/src/main/java/com/distribution/controller/AccountingController.java`

- [ ] **Step 1: `AccountingDTO.java`** (DTO phẳng để serialize bút toán + dòng sổ cái)

```java
package com.distribution.dto;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import com.distribution.service.AccountingService;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountingDTO {

    @Getter @Setter @Builder
    public static class TransactionRow {
        private Long id;
        private LocalDateTime txDate;
        private String description;
        private String sourceType;
        private Long sourceId;
        private AccountCode debitAccount;
        private AccountCode creditAccount;
        private BigDecimal amount;

        public static TransactionRow of(AccountingTransaction t) {
            return TransactionRow.builder()
                .id(t.getId()).txDate(t.getTxDate()).description(t.getDescription())
                .sourceType(t.getSourceType()).sourceId(t.getSourceId())
                .debitAccount(t.getDebitAccount()).creditAccount(t.getCreditAccount())
                .amount(t.getAmount()).build();
        }
    }

    @Getter @Setter @Builder
    public static class LedgerRow {
        private LocalDateTime txDate;
        private String description;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal runningBalance;

        public static LedgerRow of(AccountingService.LedgerLine l) {
            return LedgerRow.builder()
                .txDate(l.tx().getTxDate()).description(l.tx().getDescription())
                .debit(l.debit()).credit(l.credit()).runningBalance(l.runningBalance()).build();
        }
    }
}
```

- [ ] **Step 2: `PaymentController.java`**

```java
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
```

- [ ] **Step 3: `AccountingController.java`**

```java
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
import java.time.LocalDateTime;
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
```

- [ ] **Step 4: Kiểm tra `ApiResponse.success` có chữ ký `(data, message)`**

Run: `grep -n "public static" backend/src/main/java/com/distribution/dto/ApiResponse.java`
Expected: thấy `success(T data, String message)`. Nếu chữ ký khác → chỉnh lời gọi cho khớp.

- [ ] **Step 5: Compile + chạy app verify Flyway/security**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/distribution/dto/AccountingDTO.java backend/src/main/java/com/distribution/controller/PaymentController.java backend/src/main/java/com/distribution/controller/AccountingController.java
git commit -m "feat(accounting): payment + accounting REST controllers"
```

---

### Task 1.10: Frontend — api.js (paymentApi, accountingApi)

**Files:**
- Modify: `frontend/src/services/api.js`

- [ ] **Step 1: Thêm trước dòng `export default api;` (cuối file)**

```javascript
// ==================== Payment & Accounting API ====================
export const paymentApi = {
  getAll: (type) => api.get('/payments', { params: type ? { type } : {} }),
  create: (data) => api.post('/payments', data),
};

export const accountingApi = {
  getTransactions: (startDate, endDate) =>
    api.get('/accounting/transactions', { params: { startDate, endDate } }),
  getLedger: (account) => api.get('/accounting/ledger', { params: { account } }),
};
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/services/api.js
git commit -m "feat(accounting): add paymentApi and accountingApi clients"
```

---

### Task 1.11: Frontend — trang Thanh toán + Sổ cái

**Files:**
- Create: `frontend/src/pages/PaymentList.jsx`
- Create: `frontend/src/pages/LedgerPage.jsx`
- Modify: `frontend/src/App.jsx`

> Mirror pattern từ `frontend/src/pages/SalesInvoiceList.jsx` (cùng dùng Table + Modal + message của Ant Design + gọi api service). Đọc file đó trước để copy đúng style import/layout.

- [ ] **Step 1: `PaymentList.jsx`**

```jsx
import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, Select, InputNumber, DatePicker, Input, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { paymentApi, salesInvoiceApi } from '../services/api';

const TYPE_TAG = {
  RECEIPT: { color: 'green', label: 'Phiếu thu' },
  DISBURSEMENT: { color: 'volcano', label: 'Phiếu chi' },
};

export default function PaymentList() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [invoices, setInvoices] = useState([]);
  const [form] = Form.useForm();

  const load = async () => {
    setLoading(true);
    try {
      const res = await paymentApi.getAll();
      setData(res.data || []);
    } catch (e) { message.error(e.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const openModal = async () => {
    form.resetFields();
    form.setFieldsValue({ type: 'RECEIPT', paymentDate: dayjs() });
    try {
      const res = await salesInvoiceApi.getUnpaid();
      setInvoices(res.data || []);
    } catch { setInvoices([]); }
    setOpen(true);
  };

  const submit = async () => {
    const v = await form.validateFields();
    try {
      await paymentApi.create({
        type: v.type,
        amount: v.amount,
        paymentDate: v.paymentDate.format('YYYY-MM-DD'),
        method: v.method,
        salesInvoiceId: v.type === 'RECEIPT' ? v.salesInvoiceId : null,
        note: v.note,
      });
      message.success('Đã tạo phiếu');
      setOpen(false);
      load();
    } catch (e) { message.error(e.message); }
  };

  const columns = [
    { title: 'Mã phiếu', dataIndex: 'code' },
    { title: 'Loại', dataIndex: 'type', render: (t) => <Tag color={TYPE_TAG[t]?.color}>{TYPE_TAG[t]?.label || t}</Tag> },
    { title: 'Số tiền', dataIndex: 'amount', align: 'right', render: (a) => Number(a).toLocaleString('vi-VN') },
    { title: 'Ngày', dataIndex: 'paymentDate' },
    { title: 'Phương thức', dataIndex: 'method' },
    { title: 'Ghi chú', dataIndex: 'note' },
  ];

  return (
    <Card
      title="Phiếu thu / chi"
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={openModal}>Tạo phiếu</Button>}
    >
      <Table rowKey="id" loading={loading} dataSource={data} columns={columns} />
      <Modal title="Tạo phiếu thu/chi" open={open} onOk={submit} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="type" label="Loại phiếu" rules={[{ required: true }]}>
            <Select options={[{ value: 'RECEIPT', label: 'Phiếu thu (khách trả)' }, { value: 'DISBURSEMENT', label: 'Phiếu chi (trả NCC)' }]} />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(p, c) => p.type !== c.type}>
            {({ getFieldValue }) => getFieldValue('type') === 'RECEIPT' && (
              <Form.Item name="salesInvoiceId" label="Hóa đơn cần thu">
                <Select
                  allowClear
                  options={invoices.map((inv) => ({ value: inv.id, label: `${inv.code} — còn nợ ${Number(inv.remainingAmount || 0).toLocaleString('vi-VN')}` }))}
                />
              </Form.Item>
            )}
          </Form.Item>
          <Form.Item name="amount" label="Số tiền" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={1} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
          <Form.Item name="paymentDate" label="Ngày" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item name="method" label="Phương thức"><Input placeholder="Tiền mặt / Chuyển khoản" /></Form.Item>
          <Form.Item name="note" label="Ghi chú"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
```

- [ ] **Step 2: `LedgerPage.jsx`**

```jsx
import React, { useEffect, useState } from 'react';
import { Card, Table, Select, Space, message } from 'antd';
import { accountingApi } from '../services/api';

const ACCOUNTS = [
  { value: 'CASH', label: 'Tiền mặt/Ngân hàng' },
  { value: 'AR', label: 'Phải thu khách hàng' },
  { value: 'AP', label: 'Phải trả nhà cung cấp' },
  { value: 'REVENUE', label: 'Doanh thu' },
  { value: 'INVENTORY', label: 'Hàng tồn kho' },
  { value: 'EXPENSE', label: 'Chi phí / Giá vốn' },
];

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function LedgerPage() {
  const [account, setAccount] = useState('AR');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = async (acc) => {
    setLoading(true);
    try {
      const res = await accountingApi.getLedger(acc);
      setRows(res.data || []);
    } catch (e) { message.error(e.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(account); }, [account]);

  const columns = [
    { title: 'Thời gian', dataIndex: 'txDate', render: (t) => (t ? t.replace('T', ' ').slice(0, 16) : '') },
    { title: 'Diễn giải', dataIndex: 'description' },
    { title: 'Nợ', dataIndex: 'debit', align: 'right', render: fmt },
    { title: 'Có', dataIndex: 'credit', align: 'right', render: fmt },
    { title: 'Số dư', dataIndex: 'runningBalance', align: 'right', render: fmt },
  ];

  return (
    <Card title="Sổ cái tài khoản">
      <Space style={{ marginBottom: 16 }}>
        Tài khoản:
        <Select value={account} onChange={setAccount} options={ACCOUNTS} style={{ width: 240 }} />
      </Space>
      <Table rowKey={(_, i) => i} loading={loading} dataSource={rows} columns={columns} />
    </Card>
  );
}
```

- [ ] **Step 3: Đăng ký route + menu trong `App.jsx`**

Thêm import (gần các import page khác, ~dòng 50):
```jsx
import PaymentList from "./pages/PaymentList";
import LedgerPage from "./pages/LedgerPage";
```
Thêm route (trong khối `<Routes>`, cạnh route `sales-invoices`):
```jsx
<Route path="/payments" element={<PaymentList />} />
<Route path="/ledger" element={<LedgerPage />} />
```
Thêm nhóm menu "Kế toán" (mảng `items` của Menu, cạnh nhóm khác). Mirror cấu trúc nhóm `sales` đã có:
```jsx
{
  key: 'accounting',
  icon: <DollarOutlined />,
  label: 'Kế toán',
  children: [
    { key: '/payments', label: 'Phiếu thu/chi' },
    { key: '/ledger', label: 'Sổ cái' },
    { key: '/dashboard/accounting', label: 'Dashboard Kế toán' },
  ],
},
```
> `DollarOutlined` import từ `@ant-design/icons` (thêm vào dòng import icon hiện có nếu chưa có). Route `/dashboard/accounting` tạo ở Phase 3.

- [ ] **Step 4: Verify chạy frontend**

Run: `cd frontend && npm run dev` → mở `/payments`, tạo 1 phiếu thu gắn hóa đơn chưa trả; kiểm tra trạng thái hóa đơn đổi sang "Thanh toán một phần/Đã thanh toán" và `/ledger` (account AR/CASH) có dòng mới.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/PaymentList.jsx frontend/src/pages/LedgerPage.jsx frontend/src/App.jsx
git commit -m "feat(accounting): payment + ledger UI pages, menu and routes"
```

---

# PHASE 2 — Vận đơn trong giao hàng

### Task 2.1: Mở rộng entity DeliveryOrder + migration V10

**Files:**
- Modify: `backend/src/main/java/com/distribution/model/DeliveryOrder.java`
- Create: `backend/src/main/resources/db/migration/V10__delivery_order_fields.sql`

- [ ] **Step 1: Thêm field vào `DeliveryOrder.java`**

Sau `private SalesOrder salesOrder;` (trước dấu `}` đóng class), thêm:

```java
    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "planned_date")
    private java.time.LocalDate plannedDate;

    @Column(name = "goods_issue_id")
    private Long goodsIssueId;
```

- [ ] **Step 2: Migration `V10__delivery_order_fields.sql`**

```sql
-- V10: bổ sung thông tin người nhận / ngày giao / liên kết phiếu xuất cho vận đơn
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS recipient_name  VARCHAR(255);
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS recipient_phone VARCHAR(50);
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS planned_date    DATE;
ALTER TABLE delivery_order ADD COLUMN IF NOT EXISTS goods_issue_id  BIGINT;
```

> Kiểm tra trước tên bảng thật: `grep -n '@Table' backend/src/main/java/com/distribution/model/DeliveryOrder.java` → hiện là `delivery_order`. Khớp tên trong SQL.

- [ ] **Step 3: Chạy app verify Flyway V10 + validate**

Run: `cd backend && mvn -q spring-boot:run` (Ctrl+C sau "Started")
Expected: "Migrating schema ... to version 10", app start OK.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/distribution/model/DeliveryOrder.java backend/src/main/resources/db/migration/V10__delivery_order_fields.sql
git commit -m "feat(delivery): add recipient/planned-date/goods-issue fields to delivery order"
```

---

### Task 2.2: API chi tiết vận đơn (kèm mặt hàng từ phiếu xuất) + filter

**Files:**
- Modify: `backend/src/main/java/com/distribution/service/DeliveryOrderService.java`
- Modify: `backend/src/main/java/com/distribution/controller/DeliveryOrderController.java`
- Modify: `backend/src/main/java/com/distribution/dto/DeliveryOrderDTO.java`

> Đọc `DeliveryOrderDTO.java` và phần còn lại của `DeliveryOrderService.java` (mới đọc 40 dòng đầu) trước khi sửa, để biết các field DTO hiện có và cách `listAvailable()` map GI → DeliveryOrder.

- [ ] **Step 1: Thêm field chi tiết vào `DeliveryOrderDTO`**

Thêm các field (nếu chưa có) trong class DTO:
```java
    private String recipientName;
    private String recipientPhone;
    private java.time.LocalDate plannedDate;
    private Long goodsIssueId;
    private String goodsIssueCode;
    private String customerName;
    private java.util.List<Item> items;

    @lombok.Getter @lombok.Setter @lombok.Builder
    public static class Item {
        private String productCode;
        private String productName;
        private Integer quantity;
        private String unit;
    }
```

- [ ] **Step 2: Thêm method `getDetail(Long id)` vào `DeliveryOrderService`**

Logic: load `DeliveryOrder` theo id; nếu `goodsIssueId != null` thì load `GoodsIssue` (đã inject `goodsIssueRepository`) và map `GoodsIssueItem` → `DeliveryOrderDTO.Item` (productCode/productName/quantity/unit). Trả DTO đầy đủ. Ví dụ:

```java
    @Transactional
    public DeliveryOrderDTO getDetail(Long id) {
        DeliveryOrder order = deliveryOrderRepository.findById(id)
            .orElseThrow(() -> new com.distribution.exception.ResourceNotFoundException("Delivery Order", id));

        DeliveryOrderDTO.DeliveryOrderDTOBuilder b = DeliveryOrderDTO.builder()
            .id(order.getId())
            .code(order.getCode())
            .status(order.getStatus())
            .destinationAddress(order.getDestinationAddress())
            .recipientName(order.getRecipientName())
            .recipientPhone(order.getRecipientPhone())
            .plannedDate(order.getPlannedDate())
            .goodsIssueId(order.getGoodsIssueId());

        if (order.getGoodsIssue() != null || order.getGoodsIssueId() != null) {
            Long giId = order.getGoodsIssueId();
            if (giId != null) {
                goodsIssueRepository.findByIdWithItems(giId).ifPresent(gi -> {
                    b.goodsIssueCode(gi.getCode());
                    b.items(gi.getItems().stream().map(it -> DeliveryOrderDTO.Item.builder()
                        .productCode(it.getProduct().getCode())
                        .productName(it.getProduct().getName())
                        .quantity(it.getQuantity())
                        .unit(it.getUnit())
                        .build()).collect(java.util.stream.Collectors.toList()));
                });
            }
        }
        if (order.getSalesOrder() != null && order.getSalesOrder().getCustomer() != null) {
            b.customerName(order.getSalesOrder().getCustomer().getName());
        }
        return b.build();
    }
```
> Kiểm tra `GoodsIssueRepository` có `findByIdWithItems` (giống `findByIdWithItems` của các repo khác). Nếu không, thêm `@Query("... LEFT JOIN FETCH gi.items WHERE gi.id = :id")` hoặc dùng `findById` rồi truy cập items trong transaction. Kiểm tra `GoodsIssueItem` có getter `getQuantity()/getUnit()/getProduct()` (đọc `model/GoodsIssueItem.java`).

- [ ] **Step 3: Thêm endpoint detail vào controller**

```java
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryOrderDTO> detail(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryOrderService.getDetail(id));
    }
```

- [ ] **Step 4: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/service/DeliveryOrderService.java backend/src/main/java/com/distribution/controller/DeliveryOrderController.java backend/src/main/java/com/distribution/dto/DeliveryOrderDTO.java
git commit -m "feat(delivery): delivery order detail endpoint with items from goods issue"
```

---

### Task 2.3: Frontend — danh sách, chi tiết, in vận đơn

**Files:**
- Modify: `frontend/src/services/api.js`
- Create: `frontend/src/pages/DeliveryOrderList.jsx`
- Create: `frontend/src/pages/DeliveryOrderDetail.jsx`
- Create: `frontend/src/pages/WaybillPrint.jsx`
- Modify: `frontend/src/App.jsx`

- [ ] **Step 1: Thêm `deliveryOrderApi` vào `api.js`** (trước `export default`)

```javascript
// ==================== Delivery Order (Vận đơn) API ====================
export const deliveryOrderApi = {
  getAll: () => api.get('/delivery-orders'),
  getById: (id) => api.get(`/delivery-orders/${id}`),
};
```

- [ ] **Step 2: `DeliveryOrderList.jsx`** (mirror `DeliveryPlanList.jsx` style)

```jsx
import React, { useEffect, useState } from 'react';
import { Card, Table, Input, Tag, Button, message } from 'antd';
import { Link } from 'react-router-dom';
import { deliveryOrderApi } from '../services/api';
import { getStatusConfig } from '../services/deliveryStatus';

export default function DeliveryOrderList() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [q, setQ] = useState('');

  const load = async () => {
    setLoading(true);
    try { const res = await deliveryOrderApi.getAll(); setData(res.data || []); }
    catch (e) { message.error(e.message); }
    finally { setLoading(false); }
  };
  useEffect(() => { load(); }, []);

  const filtered = data.filter((d) =>
    !q || (d.code || '').toLowerCase().includes(q.toLowerCase()) ||
    (d.destinationAddress || '').toLowerCase().includes(q.toLowerCase()));

  const columns = [
    { title: 'Mã vận đơn', dataIndex: 'code', render: (t, r) => <Link to={`/delivery-orders/${r.id}`}>{t}</Link> },
    { title: 'Địa chỉ giao', dataIndex: 'destinationAddress' },
    { title: 'Trạng thái', dataIndex: 'status', render: (s) => { const c = getStatusConfig(s); return <Tag color={c.color}>{c.label}</Tag>; } },
    { title: '', key: 'action', render: (_, r) => <Link to={`/delivery-orders/${r.id}`}><Button size="small">Chi tiết</Button></Link> },
  ];

  return (
    <Card title="Vận đơn" extra={<Input.Search placeholder="Tìm mã / địa chỉ" allowClear style={{ width: 280 }} onChange={(e) => setQ(e.target.value)} />}>
      <Table rowKey="id" loading={loading} dataSource={filtered} columns={columns} />
    </Card>
  );
}
```

- [ ] **Step 3: `DeliveryOrderDetail.jsx`**

```jsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Descriptions, Table, Tag, Button, Space, message } from 'antd';
import { PrinterOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { deliveryOrderApi } from '../services/api';
import { getStatusConfig } from '../services/deliveryStatus';

export default function DeliveryOrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try { const res = await deliveryOrderApi.getById(id); setData(res.data); }
      catch (e) { message.error(e.message); }
      finally { setLoading(false); }
    })();
  }, [id]);

  const itemCols = [
    { title: 'Mã SP', dataIndex: 'productCode' },
    { title: 'Tên sản phẩm', dataIndex: 'productName' },
    { title: 'Số lượng', dataIndex: 'quantity', align: 'right' },
    { title: 'ĐVT', dataIndex: 'unit' },
  ];

  if (!data) return <Card loading={loading} />;
  const sc = getStatusConfig(data.status);

  return (
    <Card
      loading={loading}
      title={`Vận đơn ${data.code}`}
      extra={
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/delivery-orders')}>Quay lại</Button>
          <Button type="primary" icon={<PrinterOutlined />} onClick={() => window.open(`/delivery-orders/${id}/print`, '_blank')}>In vận đơn</Button>
        </Space>
      }
    >
      <Descriptions bordered column={2} size="small">
        <Descriptions.Item label="Mã vận đơn">{data.code}</Descriptions.Item>
        <Descriptions.Item label="Trạng thái"><Tag color={sc.color}>{sc.label}</Tag></Descriptions.Item>
        <Descriptions.Item label="Khách hàng">{data.customerName || '-'}</Descriptions.Item>
        <Descriptions.Item label="Phiếu xuất">{data.goodsIssueCode || '-'}</Descriptions.Item>
        <Descriptions.Item label="Người nhận">{data.recipientName || '-'}</Descriptions.Item>
        <Descriptions.Item label="SĐT">{data.recipientPhone || '-'}</Descriptions.Item>
        <Descriptions.Item label="Ngày giao dự kiến">{data.plannedDate || '-'}</Descriptions.Item>
        <Descriptions.Item label="Địa chỉ giao" span={2}>{data.destinationAddress || '-'}</Descriptions.Item>
      </Descriptions>
      <Table style={{ marginTop: 16 }} rowKey={(_, i) => i} dataSource={data.items || []} columns={itemCols} pagination={false} title={() => 'Danh sách mặt hàng'} />
    </Card>
  );
}
```

- [ ] **Step 4: `WaybillPrint.jsx`** (trang in tối giản + print CSS)

```jsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { deliveryOrderApi } from '../services/api';

const fmtDate = (d) => d || '';

export default function WaybillPrint() {
  const { id } = useParams();
  const [data, setData] = useState(null);

  useEffect(() => {
    (async () => {
      const res = await deliveryOrderApi.getById(id);
      setData(res.data);
      setTimeout(() => window.print(), 400);
    })();
  }, [id]);

  if (!data) return <p style={{ padding: 24 }}>Đang tải vận đơn...</p>;

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: 24, fontFamily: 'Arial, sans-serif', color: '#000' }}>
      <style>{`@media print { button { display: none; } @page { margin: 12mm; } }`}</style>
      <h2 style={{ textAlign: 'center', margin: 0 }}>PHIẾU VẬN ĐƠN</h2>
      <p style={{ textAlign: 'center', marginTop: 4 }}>Mã: <strong>{data.code}</strong></p>
      <table style={{ width: '100%', marginTop: 16 }}>
        <tbody>
          <tr><td><strong>Khách hàng:</strong> {data.customerName || '-'}</td><td><strong>Phiếu xuất:</strong> {data.goodsIssueCode || '-'}</td></tr>
          <tr><td><strong>Người nhận:</strong> {data.recipientName || '-'}</td><td><strong>SĐT:</strong> {data.recipientPhone || '-'}</td></tr>
          <tr><td colSpan={2}><strong>Địa chỉ giao:</strong> {data.destinationAddress || '-'}</td></tr>
          <tr><td colSpan={2}><strong>Ngày giao dự kiến:</strong> {fmtDate(data.plannedDate)}</td></tr>
        </tbody>
      </table>
      <table border={1} cellPadding={6} style={{ width: '100%', marginTop: 16, borderCollapse: 'collapse' }}>
        <thead>
          <tr><th>STT</th><th>Mã SP</th><th>Tên sản phẩm</th><th>SL</th><th>ĐVT</th></tr>
        </thead>
        <tbody>
          {(data.items || []).map((it, i) => (
            <tr key={i}><td style={{ textAlign: 'center' }}>{i + 1}</td><td>{it.productCode}</td><td>{it.productName}</td><td style={{ textAlign: 'right' }}>{it.quantity}</td><td>{it.unit}</td></tr>
          ))}
        </tbody>
      </table>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 48 }}>
        <div style={{ textAlign: 'center' }}>Người giao hàng<br /><br /><br />(Ký, ghi rõ họ tên)</div>
        <div style={{ textAlign: 'center' }}>Người nhận hàng<br /><br /><br />(Ký, ghi rõ họ tên)</div>
      </div>
      <button style={{ marginTop: 24 }} onClick={() => window.print()}>In lại</button>
    </div>
  );
}
```

- [ ] **Step 5: Routes + menu trong `App.jsx`**

Import:
```jsx
import DeliveryOrderList from "./pages/DeliveryOrderList";
import DeliveryOrderDetail from "./pages/DeliveryOrderDetail";
import WaybillPrint from "./pages/WaybillPrint";
```
Routes (cạnh route delivery-plans):
```jsx
<Route path="/delivery-orders" element={<DeliveryOrderList />} />
<Route path="/delivery-orders/:id" element={<DeliveryOrderDetail />} />
<Route path="/delivery-orders/:id/print" element={<WaybillPrint />} />
```
Thêm mục `{ key: '/delivery-orders', label: 'Vận đơn' }` vào children của nhóm menu "Giao hàng" (nhóm chứa delivery-plans).

> Trang `/delivery-orders/:id/print` nên render KHÔNG dùng layout sidebar. Nếu `App.jsx` bọc mọi route trong Layout chung, đặt route print NGOÀI layout (trước/ngoài khối Layout) để phiếu in sạch. Đọc cấu trúc `<Routes>` trong App.jsx để đặt đúng chỗ.

- [ ] **Step 6: Verify**

Run: `cd frontend && npm run dev` → vào `/delivery-orders`, mở 1 vận đơn, bấm "In vận đơn" → tab mới hiện phiếu + hộp thoại in.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/services/api.js frontend/src/pages/DeliveryOrderList.jsx frontend/src/pages/DeliveryOrderDetail.jsx frontend/src/pages/WaybillPrint.jsx frontend/src/App.jsx
git commit -m "feat(delivery): delivery order list, detail and printable waybill"
```

---

# PHASE 3 — Dashboard cho từng cụm

Backend thêm 5 endpoint tổng hợp; frontend 5 trang. Giữ `DashboardPage` tổng quan nguyên trạng.

### Task 3.1: DTO + service tổng hợp theo cụm

**Files:**
- Modify: `backend/src/main/java/com/distribution/dto/DashboardDTO.java`
- Modify: `backend/src/main/java/com/distribution/service/DashboardService.java`
- Modify: `backend/src/main/java/com/distribution/service/impl/DashboardServiceImpl.java`

> Đọc `DashboardDTO.java` và `DashboardServiceImpl.java` trước. Tái dùng repository có sẵn (PO/SO/Inventory/InventoryLot/DeliveryTripRoute/Payment/AccountingTransaction). Mỗi cụm trả 1 DTO gồm vài KPI (số) + 1-2 list cho biểu đồ (label + value).

- [ ] **Step 1: Thêm nested DTO cho từng cụm trong `DashboardDTO`**

Thêm các static class (mẫu cho 1 cụm — lặp tương tự cho các cụm còn lại với field phù hợp):

```java
    @Getter @Setter @Builder
    public static class ClusterChartPoint { private String label; private java.math.BigDecimal value; }

    @Getter @Setter @Builder
    public static class PurchaseDashboard {
        private long totalPO;
        private long pendingApproval;
        private long pendingReceipt;
        private java.math.BigDecimal purchaseValueThisMonth;
        private java.util.List<ClusterChartPoint> poByStatus;     // label=status, value=count
        private java.util.List<ClusterChartPoint> topSuppliers;   // label=supplier, value=amount
    }

    @Getter @Setter @Builder
    public static class SalesDashboard {
        private long totalSO;
        private java.math.BigDecimal revenueThisMonth;
        private java.util.List<ClusterChartPoint> soByStatus;
        private java.util.List<ClusterChartPoint> topCustomers;
    }

    @Getter @Setter @Builder
    public static class InventoryDashboard {
        private java.math.BigDecimal totalStockValue;
        private long lowStockCount;
        private long expiringSoonCount;
        private long expiredCount;
        private java.util.List<ClusterChartPoint> stockByWarehouse;
    }

    @Getter @Setter @Builder
    public static class DeliveryDashboard {
        private long totalTrips;
        private long completedTrips;
        private double successRate;
        private java.util.List<ClusterChartPoint> tripsByStatus;
        private java.util.List<ClusterChartPoint> ordersByShipper;
    }

    @Getter @Setter @Builder
    public static class AccountingDashboard {
        private java.math.BigDecimal totalReceivable;   // số dư AR
        private java.math.BigDecimal totalPayable;      // số dư AP
        private java.math.BigDecimal cashIn;            // tổng phiếu thu
        private java.math.BigDecimal cashOut;           // tổng phiếu chi
        private long overdueInvoices;
        private java.util.List<ClusterChartPoint> cashFlowByMonth;
    }
```

- [ ] **Step 2: Khai báo 5 method trong `DashboardService`**

```java
    DashboardDTO.PurchaseDashboard getPurchaseDashboard();
    DashboardDTO.SalesDashboard getSalesDashboard();
    DashboardDTO.InventoryDashboard getInventoryDashboard();
    DashboardDTO.DeliveryDashboard getDeliveryDashboard();
    DashboardDTO.AccountingDashboard getAccountingDashboard();
```

- [ ] **Step 3: Cài đặt trong `DashboardServiceImpl`**

Cài đặt 5 method, dùng repository có sẵn. Ví dụ mẫu cho Accounting (tái dùng `AccountingService.getLedger` + `PaymentRepository`):

```java
    @Override
    @Transactional(readOnly = true)
    public DashboardDTO.AccountingDashboard getAccountingDashboard() {
        var arLedger = accountingService.getLedger(com.distribution.model.enums.AccountCode.AR);
        var apLedger = accountingService.getLedger(com.distribution.model.enums.AccountCode.AP);
        java.math.BigDecimal ar = arLedger.isEmpty() ? java.math.BigDecimal.ZERO : arLedger.get(arLedger.size()-1).runningBalance();
        java.math.BigDecimal ap = apLedger.isEmpty() ? java.math.BigDecimal.ZERO : apLedger.get(apLedger.size()-1).runningBalance().abs();
        java.math.BigDecimal cashIn = paymentRepository.findByTypeOrderByIdDesc(com.distribution.model.enums.PaymentType.RECEIPT)
            .stream().map(p -> p.getAmount()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal cashOut = paymentRepository.findByTypeOrderByIdDesc(com.distribution.model.enums.PaymentType.DISBURSEMENT)
            .stream().map(p -> p.getAmount()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        long overdue = salesInvoiceRepository.findOverdue(java.time.LocalDate.now()).size();
        return DashboardDTO.AccountingDashboard.builder()
            .totalReceivable(ar).totalPayable(ap).cashIn(cashIn).cashOut(cashOut)
            .overdueInvoices(overdue)
            .cashFlowByMonth(java.util.List.of())
            .build();
    }
```
> Inject thêm vào `DashboardServiceImpl` các dependency cần: `AccountingService accountingService`, `PaymentRepository paymentRepository`, và các repo PO/SO/Inventory/InventoryLot/DeliveryTripRoute nếu chưa có. Với 4 cụm còn lại, dùng count/group theo status từ repository tương ứng (vd `purchaseOrderRepository.findAll()` rồi group theo `getStatus()` thành `ClusterChartPoint`). Nếu repo thiếu method group, tính bằng stream trong service (đủ cho quy mô đồ án).

- [ ] **Step 4: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/dto/DashboardDTO.java backend/src/main/java/com/distribution/service/DashboardService.java backend/src/main/java/com/distribution/service/impl/DashboardServiceImpl.java
git commit -m "feat(dashboard): per-cluster aggregation services"
```

---

### Task 3.2: Endpoint dashboard từng cụm

**Files:**
- Modify: `backend/src/main/java/com/distribution/controller/DashboardController.java`

- [ ] **Step 1: Thêm 5 endpoint**

```java
    @GetMapping("/purchase")
    public ResponseEntity<ApiResponse<DashboardDTO.PurchaseDashboard>> purchase() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPurchaseDashboard(), "OK"));
    }
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<DashboardDTO.SalesDashboard>> sales() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSalesDashboard(), "OK"));
    }
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<DashboardDTO.InventoryDashboard>> inventory() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getInventoryDashboard(), "OK"));
    }
    @GetMapping("/delivery")
    public ResponseEntity<ApiResponse<DashboardDTO.DeliveryDashboard>> delivery() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDeliveryDashboard(), "OK"));
    }
    @GetMapping("/accounting")
    public ResponseEntity<ApiResponse<DashboardDTO.AccountingDashboard>> accounting() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAccountingDashboard(), "OK"));
    }
```

- [ ] **Step 2: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/controller/DashboardController.java
git commit -m "feat(dashboard): per-cluster dashboard endpoints"
```

---

### Task 3.3: Frontend — api.js + 5 trang dashboard

**Files:**
- Modify: `frontend/src/services/api.js`
- Create: `frontend/src/pages/dashboards/PurchaseDashboard.jsx` (+ Sales/Inventory/Delivery/Accounting)
- Modify: `frontend/src/App.jsx`

> Mirror pattern KPI card + Recharts từ `DashboardPage.jsx` (đã dùng `recharts` BarChart). Mỗi trang: hàng `Statistic` cards + 1-2 biểu đồ từ list `ClusterChartPoint` (label/value).

- [ ] **Step 1: Mở rộng `dashboardApi` trong `api.js`**

Trong object `dashboardApi`, thêm:
```javascript
  getPurchase: () => api.get('/dashboard/purchase'),
  getSales: () => api.get('/dashboard/sales'),
  getInventory: () => api.get('/dashboard/inventory'),
  getDelivery: () => api.get('/dashboard/delivery'),
  getAccounting: () => api.get('/dashboard/accounting'),
```

- [ ] **Step 2: `AccountingDashboard.jsx`** (mẫu đầy đủ; 4 trang còn lại làm tương tự với field tương ứng)

```jsx
import React, { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, message } from 'antd';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardApi } from '../../services/api';

const fmt = (n) => Number(n || 0).toLocaleString('vi-VN');

export default function AccountingDashboard() {
  const [d, setD] = useState(null);
  useEffect(() => {
    (async () => {
      try { const res = await dashboardApi.getAccounting(); setD(res.data); }
      catch (e) { message.error(e.message); }
    })();
  }, []);

  return (
    <div>
      <Row gutter={16}>
        <Col span={6}><Card><Statistic title="Phải thu (AR)" value={fmt(d?.totalReceivable)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Phải trả (AP)" value={fmt(d?.totalPayable)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Tổng thu" value={fmt(d?.cashIn)} /></Card></Col>
        <Col span={6}><Card><Statistic title="Tổng chi" value={fmt(d?.cashOut)} /></Card></Col>
      </Row>
      <Card title="Dòng tiền theo tháng" style={{ marginTop: 16 }}>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={(d?.cashFlowByMonth || []).map((p) => ({ label: p.label, value: Number(p.value) }))}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataIndex="label" dataKey="label" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="value" fill="#1677ff" />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </div>
  );
}
```

> 4 trang còn lại (`PurchaseDashboard`, `SalesDashboard`, `InventoryDashboard`, `DeliveryDashboard`): cùng khung — đổi `dashboardApi.getX()`, đổi tiêu đề/`Statistic` theo field của DTO tương ứng (Task 3.1 Step 1), và biểu đồ từ list `poByStatus`/`soByStatus`/`stockByWarehouse`/`tripsByStatus`.

- [ ] **Step 3: Routes + menu `App.jsx`**

Import 5 trang; thêm routes `/dashboard/{purchase|sales|inventory|delivery|accounting}`; thêm mỗi mục dashboard vào children của nhóm menu tương ứng (Mua hàng → Dashboard Mua hàng, ...). `/dashboard/accounting` đã khai báo ở Phase 1 Task 1.11.

- [ ] **Step 4: Verify + Commit**

Run: `cd frontend && npm run dev` → mở từng dashboard, KPI hiển thị số (không lỗi console).
```bash
git add frontend/src/services/api.js frontend/src/pages/dashboards/ frontend/src/App.jsx
git commit -m "feat(dashboard): per-cluster dashboard pages"
```

---

# PHASE 4 — Đo response time

### Task 4.1: Interceptor đo thời gian + expose actuator

**Files:**
- Modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/distribution/config/RequestTimingInterceptor.java`
- Create: `backend/src/main/java/com/distribution/config/WebMvcConfig.java`

- [ ] **Step 1: Expose metrics trong `application.yml`** (thêm khối top-level)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  metrics:
    tags:
      application: distribution-backend
```

- [ ] **Step 2: `RequestTimingInterceptor.java`**

```java
package com.distribution.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/** Ghi thời gian xử lý mỗi request vào Micrometer Timer "http.api.timing" (tag uri, method). */
@Component
public class RequestTimingInterceptor implements HandlerInterceptor {

    private final MeterRegistry registry;

    public RequestTimingInterceptor(MeterRegistry registry) { this.registry = registry; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("startNanos", System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Object start = req.getAttribute("startNanos");
        if (start == null) return;
        long elapsed = System.nanoTime() - (long) start;
        String uri = req.getRequestURI();
        if (!uri.startsWith("/api")) return;
        Timer.builder("http.api.timing")
            .tag("uri", uri)
            .tag("method", req.getMethod())
            .register(registry)
            .record(elapsed, TimeUnit.NANOSECONDS);
    }
}
```

- [ ] **Step 3: `WebMvcConfig.java`**

```java
package com.distribution.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestTimingInterceptor requestTimingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestTimingInterceptor).addPathPatterns("/api/**");
    }
}
```

- [ ] **Step 4: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/resources/application.yml backend/src/main/java/com/distribution/config/RequestTimingInterceptor.java backend/src/main/java/com/distribution/config/WebMvcConfig.java
git commit -m "feat(perf): request timing interceptor + expose actuator metrics"
```

---

### Task 4.2: Endpoint /api/metrics/summary

**Files:**
- Create: `backend/src/main/java/com/distribution/controller/MetricsController.java`

- [ ] **Step 1: Viết controller đọc Timer từ MeterRegistry**

```java
package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PreAuthorize("hasRole('ADMIN')")
public class MetricsController {

    private final MeterRegistry registry;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> summary() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Timer timer : registry.find("http.api.timing").timers()) {
            rows.add(Map.of(
                "uri", timer.getId().getTag("uri"),
                "method", timer.getId().getTag("method"),
                "count", timer.count(),
                "avgMs", round(timer.mean(TimeUnit.MILLISECONDS)),
                "maxMs", round(timer.max(TimeUnit.MILLISECONDS))
            ));
        }
        rows.sort((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")));
        return ResponseEntity.ok(ApiResponse.success(rows, "Metrics summary"));
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
```

> p95: Micrometer Timer mặc định không lưu percentile trừ khi bật `publishPercentiles`. Cho đồ án dùng avg + max là đủ. Nếu cần p95, thêm `.publishPercentiles(0.95)` vào Timer.builder ở interceptor và đọc qua `timer.takeSnapshot().percentileValues()` — nhưng KHÔNG bắt buộc.

- [ ] **Step 2: Compile + Commit**

Run: `cd backend && mvn -q compile` → BUILD SUCCESS
```bash
git add backend/src/main/java/com/distribution/controller/MetricsController.java
git commit -m "feat(perf): /api/metrics/summary endpoint"
```

---

### Task 4.3: Script tải + bảng số liệu cho báo cáo

**Files:**
- Create: `scripts/perf/run.sh`
- Create: `scripts/perf/README.md`

- [ ] **Step 1: `scripts/perf/run.sh`**

```bash
#!/usr/bin/env bash
# Đo response time các endpoint chính. Cần backend chạy ở localhost:8080.
# Dùng: BASE=http://localhost:8080 USER=admin PASS=admin123 N=50 ./scripts/perf/run.sh
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"
USER="${USER:-admin}"
PASS="${PASS:-admin123}"
N="${N:-50}"

TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
  -d "{\"username\":\"$USER\",\"password\":\"$PASS\"}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then echo "Login thất bại — kiểm tra USER/PASS"; exit 1; fi

ENDPOINTS=(
  "/api/purchase-orders"
  "/api/sales-orders"
  "/api/inventory"
  "/api/dashboard/summary"
  "/api/delivery-orders"
)

printf "%-32s %8s %10s %10s\n" "endpoint" "n" "avg(ms)" "max(ms)"
for ep in "${ENDPOINTS[@]}"; do
  total=0; max=0
  for i in $(seq 1 "$N"); do
    t=$(curl -s -o /dev/null -w "%{time_total}" "$BASE$ep" -H "Authorization: Bearer $TOKEN")
    ms=$(echo "$t * 1000" | bc -l)
    total=$(echo "$total + $ms" | bc -l)
    cmp=$(echo "$ms > $max" | bc -l); [ "$cmp" -eq 1 ] && max=$ms
  done
  avg=$(echo "scale=2; $total / $N" | bc -l)
  printf "%-32s %8s %10.2f %10.2f\n" "$ep" "$N" "$avg" "$max"
done
```

- [ ] **Step 2: `scripts/perf/README.md`** — hướng dẫn chạy + cách lấy bảng từ `/api/metrics/summary`

```markdown
# Đo hiệu năng (response time)

## Cách 1 — script curl
1. Chạy backend: `cd backend && mvn spring-boot:run`
2. `chmod +x scripts/perf/run.sh`
3. `N=50 ./scripts/perf/run.sh` → in bảng endpoint | n | avg(ms) | max(ms). Copy vào báo cáo (mục WP5).

## Cách 2 — đọc Micrometer
Sau khi đã tạo tải (chạy script hoặc dùng app), gọi (token ADMIN):
`curl -s http://localhost:8080/api/metrics/summary -H "Authorization: Bearer <TOKEN>"`
Trả về count/avgMs/maxMs theo từng endpoint từ Timer "http.api.timing".
```

- [ ] **Step 3: Commit**

```bash
chmod +x scripts/perf/run.sh
git add scripts/perf/run.sh scripts/perf/README.md
git commit -m "feat(perf): load script and reporting guide"
```

---

# PHASE 5 — Bộ test đại diện (WP5)

> Spring Boot test starter đã có. Dùng H2 in-memory cho integration test để không phụ thuộc Postgres. Cần thêm dependency H2 (test scope) + file `application-test.yml` tắt Flyway, bật `ddl-auto: create-drop`.

### Task 5.1: Hạ tầng test (H2 + profile test)

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/test/resources/application-test.yml`

- [ ] **Step 1: Thêm H2 vào `pom.xml`** (trong `<dependencies>`)

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: `application-test.yml`**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
app:
  jwt:
    secret: test-secret-key-for-junit-only-distribution-1234567890
    expiration-ms: 86400000
    refresh-expiration-ms: 604800000
```

- [ ] **Step 3: Verify dependency resolve**

Run: `cd backend && mvn -q -DskipTests package` → BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/pom.xml backend/src/test/resources/application-test.yml
git commit -m "test: add H2 and test profile config"
```

---

### Task 5.2: Unit test cho AccountingService.getLedger

**Files:**
- Create: `backend/src/test/java/com/distribution/service/AccountingServiceTest.java`

- [ ] **Step 1: Viết test (mock repository, không cần Spring context)**

```java
package com.distribution.service;

import com.distribution.model.AccountingTransaction;
import com.distribution.model.enums.AccountCode;
import com.distribution.repository.AccountingTransactionRepository;
import com.distribution.service.impl.AccountingServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountingServiceTest {

    private final AccountingTransactionRepository repo = mock(AccountingTransactionRepository.class);
    private final AccountingServiceImpl service = new AccountingServiceImpl(repo);

    @Test
    void ledger_computes_running_balance_for_AR() {
        // AR: 1 hóa đơn 100 (Nợ AR), 1 phiếu thu 30 (Có AR) -> số dư 70
        AccountingTransaction invoice = AccountingTransaction.builder()
            .txDate(LocalDateTime.now().minusDays(1)).description("HĐ")
            .debitAccount(AccountCode.AR).creditAccount(AccountCode.REVENUE)
            .amount(new BigDecimal("100")).build();
        AccountingTransaction receipt = AccountingTransaction.builder()
            .txDate(LocalDateTime.now()).description("Thu")
            .debitAccount(AccountCode.CASH).creditAccount(AccountCode.AR)
            .amount(new BigDecimal("30")).build();
        when(repo.findByAccount(AccountCode.AR)).thenReturn(List.of(invoice, receipt));

        List<AccountingService.LedgerLine> lines = service.getLedger(AccountCode.AR);

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).runningBalance()).isEqualByComparingTo("100");
        assertThat(lines.get(1).runningBalance()).isEqualByComparingTo("70");
    }

    @Test
    void post_skips_non_positive_amount() {
        AccountingTransaction tx = service.post(LocalDateTime.now(), "x", "GR", 1L,
            AccountCode.INVENTORY, AccountCode.AP, BigDecimal.ZERO);
        assertThat(tx).isNull();
        verify(repo, never()).save(any());
    }
}
```

- [ ] **Step 2: Run test**

Run: `cd backend && mvn -q -Dtest=AccountingServiceTest test`
Expected: PASS (2 tests). Nếu `findByAccount` trả theo thứ tự ASC khác giả định, điều chỉnh thứ tự list trong stub.

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/distribution/service/AccountingServiceTest.java
git commit -m "test: unit test for accounting ledger running balance"
```

---

### Task 5.3: Integration test luồng cốt lõi + RBAC

**Files:**
- Create: `backend/src/test/java/com/distribution/integration/CoreFlowIT.java`
- Create: `backend/src/test/java/com/distribution/security/RbacSecurityTest.java`

- [ ] **Step 1: RBAC test (MockMvc, không token → 401/403)**

```java
package com.distribution.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacSecurityTest {

    @Autowired private MockMvc mvc;

    @Test
    void protected_endpoint_without_token_is_unauthorized() throws Exception {
        mvc.perform(get("/api/accounting/transactions")
                .param("startDate", "2026-01-01").param("endDate", "2026-12-31"))
            .andExpect(status().isUnauthorized());
    }
}
```
> Kiểm tra cấu hình security trả 401 (không token) hay 403. Nếu là 403, đổi `isUnauthorized()` → `isForbidden()`. Chạy test sẽ cho biết.

- [ ] **Step 2: Integration test luồng nghiệp vụ**

```java
package com.distribution.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: context khởi động được với profile test (H2),
 * xác nhận toàn bộ bean (gồm AccountingService hook vào GR/Invoice) wire đúng.
 * Mở rộng: tạo PO→GR→SO→GI→Invoice→Payment qua service và assert tồn kho + bút toán.
 */
@SpringBootTest
@ActiveProfiles("test")
class CoreFlowIT {

    @Test
    void application_context_loads_with_accounting_wired() {
        assertThat(true).isTrue();
    }
}
```
> Bản tối thiểu = context load (đã đủ chứng minh các bean kế toán wire đúng, không vòng lặp dependency). Nếu còn thời gian, inject `PurchaseOrderService`, `GoodsReceiptService`, ... và dựng dữ liệu để assert: sau GR confirm thì `inventoryService` tăng tồn và `AccountingTransactionRepository.count()` tăng; sau `paymentService.create(RECEIPT)` thì invoice chuyển PARTIALLY_PAID/PAID.

- [ ] **Step 3: Run toàn bộ test**

Run: `cd backend && mvn -q test`
Expected: tất cả test PASS. Sửa giả định (401 vs 403) nếu cần.

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/distribution/integration/CoreFlowIT.java backend/src/test/java/com/distribution/security/RbacSecurityTest.java
git commit -m "test: core flow context IT and RBAC security test"
```

---

### Task 5.4: Cập nhật progress_datn.md

**Files:**
- Modify: `docs/progress_datn.md`

- [ ] **Step 1:** Đổi mục "Phân hệ 4: Kế toán — Tạm hoãn" thành ĐÃ HOÀN THÀNH (bút toán tự động, phiếu thu/chi, sổ cái), thêm mục Vận đơn (in được), Dashboard từng cụm, Đo hiệu năng, và bộ Test. Bỏ dòng cảnh báo "tạm hoãn".

- [ ] **Step 2: Commit**

```bash
git add docs/progress_datn.md
git commit -m "docs: update progress — accounting, waybill, per-cluster dashboards, perf, tests done"
```

---

## Self-Review (đã rà soát khi viết plan)

- **Spec coverage:** Kế toán (Phase 1), Vận đơn+in (Phase 2), Dashboard từng cụm (Phase 3), Đo response time (Phase 4), Testing (Phase 5) — đủ 5 hạng mục spec.
- **Ràng buộc Flyway/validate:** mọi entity mới đều có migration (V9, V10) trước khi chạy app — đã nêu rõ ở Task 1.3, 2.1.
- **Type consistency:** `AccountCode`/`PaymentType` dùng nhất quán; `AccountingService.post(...)` cùng chữ ký ở mọi nơi gọi (GR, Invoice, Payment); `LedgerLine` record field `tx/debit/credit/runningBalance` khớp giữa service và `AccountingDTO.LedgerRow.of`.
- **Điểm cần verify khi code (đã ghi chú inline):** chữ ký `ApiResponse.success`, `GoodsIssueRepository.findByIdWithItems`, getter của `GoodsIssueItem`, 401-vs-403, các field sẵn có của `DeliveryOrderDTO`/`DashboardDTO`.
