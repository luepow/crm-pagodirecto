package com.empresa.crm.ventas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a DetalleVenta
 * Bounded context: Ventas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearDetalleVentaRequest {

    @NotNull(message = "Producto ID is required")
    private UUID productoId;

    @NotNull(message = "Cantidad is required")
    @Min(value = 1, message = "Cantidad must be at least 1")
    private Integer cantidad;

    @NotNull(message = "Precio unitario is required")
    @Min(value = 0, message = "Precio unitario cannot be negative")
    private BigDecimal precioUnitario;

    private BigDecimal descuento;
}
