package com.empresa.crm.cuentas.dto;

import com.empresa.crm.cuentas.model.TipoCuenta;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new Cuenta
 * Bounded context: Cuentas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearCuentaRequest {

    @NotNull(message = "Tipo is required")
    private TipoCuenta tipo;

    @NotBlank(message = "Referencia tipo is required")
    private String referenciaTipo;

    @NotNull(message = "Referencia ID is required")
    private Long referenciaId;

    @NotNull(message = "Cliente ID is required")
    private Long clienteId;

    private String descripcion;

    @NotNull(message = "Monto is required")
    @Min(value = 0, message = "Monto must be positive")
    private BigDecimal monto;

    @NotNull(message = "Fecha emision is required")
    private LocalDate fechaEmision;

    @NotNull(message = "Fecha vencimiento is required")
    private LocalDate fechaVencimiento;
}
