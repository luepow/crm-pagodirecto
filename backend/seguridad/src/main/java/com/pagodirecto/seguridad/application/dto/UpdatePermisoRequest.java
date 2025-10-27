package com.pagodirecto.seguridad.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: UpdatePermisoRequest
 *
 * Request DTO para actualizar un permiso existente
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermisoRequest {

    @Size(max = 100, message = "El scope no puede exceder 100 caracteres")
    private String scope;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
}
