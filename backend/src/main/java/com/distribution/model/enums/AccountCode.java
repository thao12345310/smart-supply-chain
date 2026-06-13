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
