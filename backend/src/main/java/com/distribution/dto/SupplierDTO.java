package com.distribution.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Long id;
    private String code;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
}
