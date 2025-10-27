package com.pagodirecto.clientes.application.dto;

import com.pagodirecto.clientes.domain.DireccionTipo;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: Dirección
 *
 * Data Transfer Object para la entidad Direccion.
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
public class DireccionDTO {

    private UUID id;

    @NotNull(message = "El cliente es obligatorio")
    private UUID clienteId;

    @NotNull(message = "El tipo es obligatorio")
    private DireccionTipo tipo;

    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 255, message = "La calle no puede exceder 255 caracteres")
    private String calle;

    @Size(max = 20, message = "El número exterior no puede exceder 20 caracteres")
    private String numeroExterior;

    @Size(max = 20, message = "El número interior no puede exceder 20 caracteres")
    private String numeroInterior;

    @Size(max = 100, message = "La colonia no puede exceder 100 caracteres")
    private String colonia;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 100, message = "El estado no puede exceder 100 caracteres")
    private String estado;

    @NotBlank(message = "El código postal es obligatorio")
    @Size(max = 10, message = "El código postal no puede exceder 10 caracteres")
    private String codigoPostal;

    @NotBlank(message = "El país es obligatorio")
    @Pattern(regexp = "^[A-Z]{2}$", message = "El país debe ser un código ISO de 2 letras")
    private String pais;

    private String referencia;

    private Boolean isDefault;

    private Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
}
