package com.pagodirecto.seguridad.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: CreatePermisoRequest
 *
 * Request DTO para crear un nuevo permiso
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermisoRequest {

    @NotBlank(message = "El recurso es obligatorio")
    @Size(max = 100, message = "El recurso no puede exceder 100 caracteres")
    private String recurso;

    @NotBlank(message = "La acción es obligatoria")
    @Pattern(regexp = "CREATE|READ|UPDATE|DELETE|EXECUTE|ADMIN", 
             message = "La acción debe ser: CREATE, READ, UPDATE, DELETE, EXECUTE o ADMIN")
    private String accion;

    @Size(max = 100, message = "El scope no puede exceder 100 caracteres")
    private String scope;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
}
