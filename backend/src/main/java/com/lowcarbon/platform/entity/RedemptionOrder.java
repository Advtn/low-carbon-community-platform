package com.lowcarbon.platform.entity;

import com.lowcarbon.platform.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "redemption_orders", indexes = {
        @Index(name = "idx_order_user_time", columnList = "user_id,created_at"),
        @Index(name = "idx_order_status_time", columnList = "status,created_at"),
        @Index(name = "idx_order_status_completed", columnList = "status,completed_at")
})
public class RedemptionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private MallItem item;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer totalPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(length = 300)
    private String remark;
}
