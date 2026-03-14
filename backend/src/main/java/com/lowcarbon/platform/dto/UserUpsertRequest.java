package com.lowcarbon.platform.dto;

import com.lowcarbon.platform.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpsertRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String nickname,
        @NotNull UserRole role) {
}
