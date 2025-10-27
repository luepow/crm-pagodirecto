package com.pagodirecto.seguridad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: RefreshTokenRequest
 *
 * Solicitud de renovación de token de acceso
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de renovación de token de acceso")
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    @Schema(description = "Token de refresco", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
}
