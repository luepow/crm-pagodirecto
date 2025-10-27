package com.pagodirecto.seguridad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO: RegisterRequest
 *
 * Solicitud de registro de nuevo usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de registro de nuevo usuario")
public class RegisterRequest {

    @NotNull(message = "El ID de unidad de negocio es obligatorio")
    @Schema(description = "ID de la unidad de negocio")
    private UUID unidadNegocioId;

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 100, message = "El username debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre de usuario único", example = "juan.perez")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@pagodirecto.com")
    private String email;

    @NotBlank(message = "El password es obligatorio")
    @Size(min = 8, max = 128, message = "El password debe tener entre 8 y 128 caracteres")
    @Schema(description = "Contraseña del usuario (mínimo 8 caracteres, debe incluir mayúsculas, minúsculas, números y símbolos)", example = "P@ssw0rd123!")
    private String password;

    @Schema(description = "IDs de roles a asignar al usuario")
    private Set<UUID> roleIds;

    @Schema(description = "Habilitar autenticación de dos factores (MFA)", example = "false")
    @Builder.Default
    private Boolean mfaEnabled = false;
}
