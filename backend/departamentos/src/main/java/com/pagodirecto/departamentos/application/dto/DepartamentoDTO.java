package com.pagodirecto.departamentos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO: DepartamentoDTO
 *
 * Data Transfer Object para departamentos
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartamentoDTO {
    private UUID id;
    private UUID unidadNegocioId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private UUID parentId;
    private String parentNombre;
    private Integer nivel;
    private String path;
    private UUID jefeId;
    private String jefeNombre;
    private String emailDepartamento;
    private String telefonoDepartamento;
    private String ubicacion;
    private BigDecimal presupuestoAnual;
    private Integer numeroEmpleados;
    private Boolean activo;
    private Instant createdAt;
    private Instant updatedAt;
}
