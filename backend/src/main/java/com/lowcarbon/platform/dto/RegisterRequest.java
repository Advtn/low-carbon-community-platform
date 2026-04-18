package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在 3-50 个字符之间")
        String username,
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码应为 6 位数字")
        String verificationCode,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码至少 6 位")
        String password
) {
}

