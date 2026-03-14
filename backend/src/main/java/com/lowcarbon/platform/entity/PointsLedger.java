package com.lowcarbon.platform.entity;

import com.lowcarbon.platform.enums.LedgerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "points_ledgers", indexes = {
        @Index(name = "idx_ledger_user_time", columnList = "user_id,created_at")
})
public class PointsLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer changePoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerType type;

    private Long relatedId;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
