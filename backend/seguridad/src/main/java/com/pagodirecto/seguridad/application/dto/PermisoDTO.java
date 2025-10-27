package com.pagodirecto.seguridad.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: PermisoDTO
 *
 * Data Transfer Object para permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermisoDTO {
    private UUID id;
    private String recurso;
    private String accion;
    private String scope;
    private String descripcion;
    private Instant createdAt;
}
