package com.pagodirecto.seguridad.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO: RolWithPermisosDTO
 *
 * Data Transfer Object para roles con permisos completos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolWithPermisosDTO {
    private UUID id;
    private UUID unidadNegocioId;
    private String nombre;
    private String descripcion;
    private String departamento;
    private Integer nivelJerarquico;
    private Set<PermisoDTO> permisos;
    private Instant createdAt;
    private Instant updatedAt;
}
