package com.pagodirecto.seguridad.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: RoleDTO
 *
 * Data Transfer Object para roles
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String departamento;
    private Integer nivelJerarquico;
    private Instant createdAt;
}
