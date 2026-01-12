package com.distribution.model.enums;

/**
 * Sales Invoice Status Lifecycle:
 * DRAFT → ISSUED → PAID / CANCELLED
 * 
 * DRAFT: Initial state when invoice is created
 * ISSUED: Invoice issued to customer
 * PAID: Invoice fully paid
 * PARTIALLY_PAID: Invoice partially paid
 * CANCELLED: Invoice cancelled
 * OVERDUE: Invoice past due date
 */
public enum SalesInvoiceStatus {
    DRAFT("Draft", "Invoice created, pending issuance"),
    ISSUED("Issued", "Invoice issued to customer"),
    PARTIALLY_PAID("Partially Paid", "Invoice partially paid"),
    PAID("Paid", "Invoice fully paid"),
    CANCELLED("Cancelled", "Invoice cancelled"),
    OVERDUE("Overdue", "Invoice past due date");

    private final String displayName;
    private final String description;

    SalesInvoiceStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if invoice can be issued
     */
    public boolean canIssue() {
        return this == DRAFT;
    }

    /**
     * Check if invoice can accept payment
     */
    public boolean canReceivePayment() {
        return this == ISSUED || this == PARTIALLY_PAID || this == OVERDUE;
    }

    /**
     * Check if invoice can be cancelled
     */
    public boolean canCancel() {
        return this == DRAFT || this == ISSUED;
    }
}
