package com.empresa.crm.pagos.dto;

import com.empresa.crm.pagos.model.MetodoPago;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new Pago
 * Bounded context: Pagos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPagoRequest {

    @NotNull(message = "Venta ID is required")
    private Long ventaId;

    @NotNull(message = "Metodo pago is required")
    private MetodoPago metodoPago;

    @NotNull(message = "Monto is required")
    @Min(value = 0, message = "Monto must be positive")
    private BigDecimal monto;

    @NotNull(message = "Fecha pago is required")
    private LocalDate fechaPago;

    private String referencia;

    private String notas;
}
