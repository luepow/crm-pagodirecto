package com.pagodirecto.seguridad.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * DTO: UpdateRolRequest
 *
 * Request DTO para actualizar un rol existente
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    private String departamento;

    @Min(value = 0, message = "El nivel jerárquico debe ser mayor o igual a 0")
    @Max(value = 10, message = "El nivel jerárquico debe ser menor o igual a 10")
    private Integer nivelJerarquico;

    private Set<UUID> permisoIds;
}
