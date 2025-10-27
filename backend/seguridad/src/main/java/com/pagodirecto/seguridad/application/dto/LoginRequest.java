package com.pagodirecto.seguridad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: LoginRequest
 *
 * Solicitud de autenticación de usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de autenticación de usuario")
public class LoginRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 100, message = "El username debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre de usuario", example = "admin@pagodirecto.com")
    private String username;

    @NotBlank(message = "El password es obligatorio")
    @Size(min = 8, message = "El password debe tener al menos 8 caracteres")
    @Schema(description = "Contraseña del usuario", example = "P@ssw0rd123!")
    private String password;

    @Schema(description = "Código TOTP para autenticación de dos factores (si MFA está habilitado)", example = "123456")
    private String totpCode;
}
