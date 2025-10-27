package com.pagodirecto.clientes.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: Contacto
 *
 * Data Transfer Object para la entidad Contacto.
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
public class ContactoDTO {

    private UUID id;

    @NotNull(message = "El cliente es obligatorio")
    private UUID clienteId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    private String telefono;

    @Size(max = 50, message = "El teléfono móvil no puede exceder 50 caracteres")
    private String telefonoMovil;

    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    private String cargo;

    @Size(max = 100, message = "El departamento no puede exceder 100 caracteres")
    private String departamento;

    private Boolean isPrimary;

    private String notas;

    private Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
}
