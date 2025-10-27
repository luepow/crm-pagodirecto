package com.empresa.crm.pagos.dto;

import com.empresa.crm.pagos.model.EstadoPago;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating Pago estado
 * Bounded context: Pagos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoPagoRequest {

    @NotNull(message = "Estado is required")
    private EstadoPago estado;
}
