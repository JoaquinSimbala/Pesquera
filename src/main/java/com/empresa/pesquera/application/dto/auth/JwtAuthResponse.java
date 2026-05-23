package com.empresa.pesquera.application.dto.auth;

import java.time.Instant;

public record JwtAuthResponse(
        String tokenType,
        String username,
        String rol,
        Instant expiration
) {
}
