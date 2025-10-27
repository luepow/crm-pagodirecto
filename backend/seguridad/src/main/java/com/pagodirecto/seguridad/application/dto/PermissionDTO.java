package com.pagodirecto.seguridad.application.dto;

import com.pagodirecto.seguridad.domain.PermissionAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: PermissionDTO
 *
 * Representación de un permiso del sistema
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Representación de un permiso del sistema")
public class PermissionDTO {

    @Schema(description = "ID único del permiso")
    private UUID id;

    @Schema(description = "Recurso al que aplica el permiso", example = "clientes")
    private String recurso;

    @Schema(description = "Acción permitida", example = "READ")
    private PermissionAction accion;

    @Schema(description = "Alcance del permiso", example = "clients:read")
    private String scope;

    @Schema(description = "Descripción del permiso", example = "Permite consultar información de clientes")
    private String descripcion;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "ID del usuario que creó este registro")
    private UUID createdBy;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "ID del usuario que actualizó este registro")
    private UUID updatedBy;
}
