package com.pagodirecto.departamentos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO: UpdateDepartamentoRequest
 *
 * Request DTO para actualizar un departamento existente
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartamentoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    private UUID parentId;

    private UUID jefeId;

    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String emailDepartamento;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    private String telefonoDepartamento;

    @Size(max = 255, message = "La ubicación no puede exceder 255 caracteres")
    private String ubicacion;

    @DecimalMin(value = "0.0", message = "El presupuesto debe ser mayor o igual a 0")
    private BigDecimal presupuestoAnual;

    @Min(value = 0, message = "El número de empleados debe ser mayor o igual a 0")
    private Integer numeroEmpleados;

    private Boolean activo;
}
