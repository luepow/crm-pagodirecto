package com.pagodirecto.seguridad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO: LoginResponse
 *
 * Respuesta de autenticación exitosa con tokens JWT
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de autenticación exitosa con tokens JWT")
public class LoginResponse {

    @Schema(description = "Token de acceso JWT (5 minutos de vigencia)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token de refresco (30 días de vigencia)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Tiempo de expiración del access token en segundos", example = "300")
    @Builder.Default
    private Long expiresIn = 300L; // 5 minutos

    @Schema(description = "ID del usuario autenticado")
    private UUID userId;

    @Schema(description = "Nombre de usuario")
    private String username;

    @Schema(description = "Email del usuario")
    private String email;

    @Schema(description = "Roles asignados al usuario")
    private Set<String> roles;

    @Schema(description = "Permisos del usuario (formato: recurso:accion)")
    private Set<String> permissions;

    @Schema(description = "Timestamp de último acceso")
    private Instant lastAccess;

    @Schema(description = "Indica si el usuario tiene MFA habilitado")
    private Boolean mfaEnabled;

    @Schema(description = "ID de la unidad de negocio del usuario")
    private UUID unidadNegocioId;
}
