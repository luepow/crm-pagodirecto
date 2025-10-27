package com.pagodirecto.seguridad.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: UpdateProfileRequest
 *
 * Request para actualizar el perfil del usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para actualizar perfil de usuario")
public class UpdateProfileRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 255, message = "El correo no puede exceder 255 caracteres")
    @Schema(description = "Correo electrónico", example = "juan.perez@pagodirecto.com")
    private String email;

    @Size(max = 100, message = "El nombre completo no puede exceder 100 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez García")
    private String nombreCompleto;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Schema(description = "Teléfono de contacto", example = "+52 55 1234 5678")
    private String telefono;

    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    @Schema(description = "Cargo o posición", example = "Gerente de Ventas")
    private String cargo;

    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    @Schema(description = "Departamento al que pertenece", example = "Ventas")
    private String departamento;

    @Schema(description = "URL de la foto de perfil")
    private String photoUrl;
}
