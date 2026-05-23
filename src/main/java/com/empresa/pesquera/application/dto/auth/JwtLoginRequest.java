package com.empresa.pesquera.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record JwtLoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String username,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
