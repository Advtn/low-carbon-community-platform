package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(1) Integer pointsCost,
        @NotNull @Min(0) Integer stock,
        @NotNull Boolean enabled) {
}
