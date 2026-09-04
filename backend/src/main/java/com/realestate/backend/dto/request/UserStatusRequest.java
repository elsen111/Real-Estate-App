package com.realestate.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull Boolean enabled
) {
}
