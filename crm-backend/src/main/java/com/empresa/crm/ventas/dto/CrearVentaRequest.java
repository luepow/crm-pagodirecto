package com.empresa.crm.ventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a new Venta
 * Bounded context: Ventas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearVentaRequest {

    @NotNull(message = "Cliente ID is required")
    private Long clienteId;

    @NotNull(message = "Fecha venta is required")
    private LocalDate fechaVenta;

    @NotEmpty(message = "Detalles are required")
    @Valid
    private List<CrearDetalleVentaRequest> detalles;

    private BigDecimal descuento;

    private BigDecimal impuestos;

    private String notas;
}
