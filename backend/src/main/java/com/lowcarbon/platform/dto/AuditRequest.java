package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.NotNull;

public record AuditRequest(@NotNull Boolean approved, String remark) {
}
