package com.lowcarbon.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lowcarbon.platform.enums.LedgerType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("points_ledgers")
public class PointsLedger {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("change_points")
    private Integer changePoints;

    private LedgerType type;

    @TableField("related_id")
    private Long relatedId;

    private String description;

    @TableField("balance_after")
    private Integer balanceAfter;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

