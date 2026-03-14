package com.lowcarbon.platform.dto;

import com.lowcarbon.platform.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status, String remark) {
}
