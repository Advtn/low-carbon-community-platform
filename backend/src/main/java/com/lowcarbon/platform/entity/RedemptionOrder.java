package com.lowcarbon.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lowcarbon.platform.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("redemption_orders")
public class RedemptionOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("item_id")
    private Long itemId;

    private Integer quantity;

    @TableField("total_points")
    private Integer totalPoints;

    private OrderStatus status = OrderStatus.PENDING;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @TableField("completed_at")
    private LocalDateTime completedAt;

    private String remark;
}

