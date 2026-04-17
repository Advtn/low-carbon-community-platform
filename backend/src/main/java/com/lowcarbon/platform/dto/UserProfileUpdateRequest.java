package com.lowcarbon.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 50) String fullName,
        @NotBlank @Size(max = 50) String nickname,
        @Size(max = 20) String gender,
        @Email @Size(max = 120) String email,
        @Size(max = 30) String phone,
        @Size(max = 255) String address,
        @Size(max = 500) String bio,
        @Size(max = 80) String city,
        @Size(max = 120) String organization,
        @Size(max = 300) String tags
) {
}
