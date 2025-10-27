package com.empresa.crm.cuentas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for applying payment to a Cuenta
 * Bounded context: Cuentas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AplicarPagoRequest {

    @NotNull(message = "Monto pago is required")
    @Min(value = 0, message = "Monto pago must be positive")
    private BigDecimal montoPago;
}
