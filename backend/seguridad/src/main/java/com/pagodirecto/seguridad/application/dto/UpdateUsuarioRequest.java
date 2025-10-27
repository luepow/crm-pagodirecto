package com.pagodirecto.seguridad.application.dto;

import com.pagodirecto.seguridad.domain.UsuarioStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO: UpdateUsuarioRequest
 *
 * Request DTO para actualizar un usuario existente
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsuarioRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    @Size(max = 100, message = "El nombre completo no puede exceder 100 caracteres")
    private String nombreCompleto;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    private String cargo;

    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    private String departamento;

    @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
    private String photoUrl;

    private UsuarioStatus status;

    private Set<UUID> roleIds;
}
