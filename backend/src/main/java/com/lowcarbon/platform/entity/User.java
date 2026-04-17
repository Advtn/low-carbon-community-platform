package com.lowcarbon.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lowcarbon.platform.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    @TableField("full_name")
    private String fullName;

    private String gender;

    private String email;

    private String phone;

    private String address;

    private String bio;

    private String city;

    private String organization;

    private String tags;

    private UserRole role = UserRole.RESIDENT;

    @TableField("total_points")
    private Integer totalPoints = 0;

    @TableField("total_carbon_reduction")
    private Double totalCarbonReduction = 0.0;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
