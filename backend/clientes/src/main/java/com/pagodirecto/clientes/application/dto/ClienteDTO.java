package com.pagodirecto.clientes.application.dto;

import com.pagodirecto.clientes.domain.ClienteStatus;
import com.pagodirecto.clientes.domain.ClienteTipo;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: Cliente
 *
 * Data Transfer Object para la entidad Cliente.
 * Incluye validaciones Jakarta Bean Validation.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    private UUID id;

    @NotNull(message = "La unidad de negocio es obligatoria")
    private UUID unidadNegocioId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    private String telefono;

    @NotNull(message = "El tipo es obligatorio")
    private ClienteTipo tipo;

    @Size(max = 20, message = "El RFC no puede exceder 20 caracteres")
    private String rfc;

    @Size(max = 255, message = "La razón social no puede exceder 255 caracteres")
    private String razonSocial;

    @NotNull(message = "El status es obligatorio")
    private ClienteStatus status;

    @Size(max = 50, message = "El segmento no puede exceder 50 caracteres")
    private String segmento;

    @Size(max = 50, message = "La fuente no puede exceder 50 caracteres")
    private String fuente;

    private UUID propietarioId;

    private String notas;

    private Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
}
