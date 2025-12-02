package com.distribution.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer quantity;
    private Double price;
    private Long supplierId;
}
