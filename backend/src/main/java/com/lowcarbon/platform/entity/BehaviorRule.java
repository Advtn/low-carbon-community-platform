package com.lowcarbon.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("behavior_rules")
public class BehaviorRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    @TableField("points_per_action")
    private Integer pointsPerAction;

    @TableField("carbon_reduction_per_action")
    private Double carbonReductionPerAction;

    @TableField("daily_limit")
    private Integer dailyLimit;

    private Boolean active = true;
}

