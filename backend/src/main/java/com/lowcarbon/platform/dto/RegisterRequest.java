package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在 3-50 个字符之间")
        String username,
        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 50, message = "昵称长度需在 2-50 个字符之间")
        String nickname,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码至少 6 位")
        String password
) {
}
