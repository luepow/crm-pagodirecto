package com.empresa.crm.ventas.dto;

import com.empresa.crm.ventas.model.EstadoVenta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating Venta estado
 * Bounded context: Ventas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoVentaRequest {

    @NotNull(message = "Estado is required")
    private EstadoVenta estado;
}
