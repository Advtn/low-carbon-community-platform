package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BehaviorReportCreateRequest(
        @NotNull Long ruleId,
        @NotNull @Min(1) Integer quantity,
        String proofText,
        String proofImageUrl) {
}
