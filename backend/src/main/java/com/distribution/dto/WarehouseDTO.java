package com.distribution.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {
    private Long id;
    private String code;
    private String name;
    private String location;
    private String description;
}
