package com.pagodirecto.seguridad.application.dto;

import com.pagodirecto.seguridad.domain.UsuarioStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO: UsuarioDTO
 *
 * Data Transfer Object para usuarios (completo)
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private UUID id;
    private UUID unidadNegocioId;
    private String username;
    private String email;
    private String nombreCompleto;
    private String telefono;
    private String cargo;
    private String departamento;
    private String photoUrl;
    private Boolean mfaEnabled;
    private UsuarioStatus status;
    private Instant ultimoAcceso;
    private Integer intentosFallidos;
    private Instant bloqueadoHasta;
    private Set<RoleDTO> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
