package com.pagodirecto.oportunidades.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO: Oportunidad
 *
 * Data Transfer Object para la entidad Oportunidad.
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
public class OportunidadDTO {

    private UUID id;

    @NotNull(message = "La unidad de negocio es obligatoria")
    private UUID unidadNegocioId;

    @NotNull(message = "El cliente es obligatorio")
    private UUID clienteId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String titulo;

    private String descripcion;

    @NotNull(message = "El valor estimado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor estimado debe ser mayor a 0")
    private BigDecimal valorEstimado;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe ser un código de 3 letras")
    private String moneda;

    @NotNull(message = "La probabilidad es obligatoria")
    @DecimalMin(value = "0.0", message = "La probabilidad debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", message = "La probabilidad debe ser menor o igual a 100")
    private BigDecimal probabilidad;

    @NotNull(message = "La etapa es obligatoria")
    private UUID etapaId;

    private LocalDate fechaCierreEstimada;

    private LocalDate fechaCierreReal;

    @NotNull(message = "El propietario es obligatorio")
    private UUID propietarioId;

    @Size(max = 50, message = "La fuente no puede exceder 50 caracteres")
    private String fuente;

    private String motivoPerdida;

    private BigDecimal valorPonderado;

    private Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;

    // Información del cliente (desnormalizada para mostrar en listados)
    private String clienteNombre;
    private String etapaNombre;
    private String propietarioNombre;
}
