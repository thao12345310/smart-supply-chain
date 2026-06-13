package com.distribution.model.enums;

/** RECEIPT = phiếu thu (khách trả tiền); DISBURSEMENT = phiếu chi (trả NCC). */
public enum PaymentType {
    RECEIPT("Phiếu thu"),
    DISBURSEMENT("Phiếu chi");

    private final String displayName;
    PaymentType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
