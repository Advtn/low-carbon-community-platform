package com.lowcarbon.platform.dto;

import com.lowcarbon.platform.enums.UserRole;

public record LoginResponse(String token,
                            Long userId,
                            String username,
                            String nickname,
                            UserRole role,
                            Integer totalPoints,
                            Double totalCarbonReduction) {
}
