package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleRequest(
        @NotBlank String name,
        String description,
        @NotNull @Min(0) Integer pointsPerAction,
        @NotNull @Min(0) Double carbonReductionPerAction,
        @NotNull @Min(1) Integer dailyLimit,
        @NotNull Boolean active) {
}
