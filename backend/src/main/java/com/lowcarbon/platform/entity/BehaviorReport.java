package com.lowcarbon.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lowcarbon.platform.enums.ReportStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("behavior_reports")
public class BehaviorReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("rule_id")
    private Long ruleId;

    private Integer quantity;

    @TableField("proof_text")
    private String proofText;

    @TableField("proof_image_url")
    private String proofImageUrl;

    private ReportStatus status = ReportStatus.PENDING;

    @TableField("submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @TableField("audited_at")
    private LocalDateTime auditedAt;

    @TableField("auditor_id")
    private Long auditorId;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("granted_points")
    private Integer grantedPoints;

    @TableField("granted_carbon")
    private Double grantedCarbon;
}

