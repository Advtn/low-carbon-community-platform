package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RedeemRequest(@NotNull Long itemId, @NotNull @Min(1) Integer quantity) {
}
