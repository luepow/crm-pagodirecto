package com.pagodirecto.tareas.application.dto;

import com.pagodirecto.tareas.domain.PrioridadTarea;
import com.pagodirecto.tareas.domain.StatusTarea;
import com.pagodirecto.tareas.domain.TipoTarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para transferencia de datos de Tareas
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaDTO {

    private UUID id;

    @NotNull(message = "La unidad de negocio es obligatoria")
    private UUID unidadNegocioId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String titulo;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de tarea es obligatorio")
    private TipoTarea tipo;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadTarea prioridad;

    @NotNull(message = "El status es obligatorio")
    private StatusTarea status;

    private LocalDate fechaVencimiento;

    private Instant fechaCompletada;

    @NotNull(message = "El usuario asignado es obligatorio")
    private UUID asignadoA;

    private String asignadoNombre;

    private String relacionadoTipo;

    private UUID relacionadoId;

    private String relacionadoNombre;

    private Boolean vencida;

    private Instant createdAt;

    private UUID createdBy;

    private String createdByNombre;

    private Instant updatedAt;

    private UUID updatedBy;
}
