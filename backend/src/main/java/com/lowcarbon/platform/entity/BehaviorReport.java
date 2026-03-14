package com.lowcarbon.platform.entity;

import com.lowcarbon.platform.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "behavior_reports", indexes = {
        @Index(name = "idx_report_user_rule_time", columnList = "user_id,rule_id,submitted_at"),
        @Index(name = "idx_report_status_time", columnList = "status,submitted_at"),
        @Index(name = "idx_report_audit_time", columnList = "status,audited_at")
})
public class BehaviorReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private BehaviorRule rule;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String proofText;

    @Column(length = 500)
    private String proofImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    private Long auditorId;

    @Column(length = 300)
    private String auditRemark;

    private Integer grantedPoints;

    private Double grantedCarbon;
}
