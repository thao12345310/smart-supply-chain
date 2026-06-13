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
