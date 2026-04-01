package com.datn.backend.entity;

import com.datn.backend.entity.enums.PaymentMethod;
import com.datn.backend.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_code", unique = true)
    private Long orderCode;

    @Column(name = "payment_link_id")
    private String paymentLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private VipPackage vipPackage;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 7)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
