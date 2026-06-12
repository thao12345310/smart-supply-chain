package com.distribution.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotDisposalRequest {
    // Lý do hủy (mặc định "Hết hạn sử dụng" nếu bỏ trống)
    private String reason;
    private Long disposedBy;
}
