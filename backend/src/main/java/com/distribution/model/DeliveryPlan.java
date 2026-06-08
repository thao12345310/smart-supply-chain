package com.distribution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "delivery_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private LocalDate createdDate;
    private LocalDate plannedDate; // ngày giao dự kiến
    private String description;
    private String notes;          // ghi chú thêm
    private String status; // Created, InProgress, Completed

    // Số vận đơn thuộc đợt — không lưu DB, set ở tầng controller để hiển thị danh sách
    @Transient
    private Long orderCount;

    @JsonIgnore
    @OneToMany(mappedBy = "deliveryPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryPlanOrder> orders;

    @JsonIgnore
    @OneToMany(mappedBy = "deliveryPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryPlanShipper> shippers;
}
