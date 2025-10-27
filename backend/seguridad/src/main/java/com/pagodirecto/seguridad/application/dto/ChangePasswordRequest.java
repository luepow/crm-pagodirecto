package com.pagodirecto.seguridad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ChangePasswordRequest
 *
 * Solicitud de cambio de contraseña
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de cambio de contraseña")
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    @Schema(description = "Contraseña actual del usuario", example = "OldP@ssw0rd!")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 128, message = "La nueva contraseña debe tener entre 8 y 128 caracteres")
    @Schema(description = "Nueva contraseña (mínimo 8 caracteres, debe incluir mayúsculas, minúsculas, números y símbolos)", example = "NewP@ssw0rd123!")
    private String newPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    @Schema(description = "Confirmación de la nueva contraseña", example = "NewP@ssw0rd123!")
    private String confirmPassword;
}
