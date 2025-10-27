package com.pagodirecto.seguridad.application.dto;

import com.pagodirecto.seguridad.domain.UsuarioStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO: UserDTO
 *
 * Representación de un usuario del sistema
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Representación de un usuario del sistema")
public class UserDTO {

    @Schema(description = "ID único del usuario")
    private UUID id;

    @Schema(description = "ID de la unidad de negocio")
    private UUID unidadNegocioId;

    @Schema(description = "Nombre de usuario", example = "juan.perez")
    private String username;

    @Schema(description = "Correo electrónico", example = "juan.perez@pagodirecto.com")
    private String email;

    @Schema(description = "Estado del usuario", example = "ACTIVE")
    private UsuarioStatus status;

    @Schema(description = "MFA habilitado", example = "false")
    private Boolean mfaEnabled;

    @Schema(description = "Último acceso al sistema")
    private Instant ultimoAcceso;

    @Schema(description = "Intentos fallidos de login", example = "0")
    private Integer intentosFallidos;

    @Schema(description = "Bloqueado hasta (timestamp)")
    private Instant bloqueadoHasta;

    @Schema(description = "Roles asignados al usuario")
    private Set<RoleDTO> roles;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "ID del usuario que creó este registro")
    private UUID createdBy;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "ID del usuario que actualizó este registro")
    private UUID updatedBy;
}
