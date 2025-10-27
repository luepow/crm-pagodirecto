package com.empresa.crm.productos.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adjusting product stock.
 * Application layer - Stock management use case
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjustarStockRequest {

    @NotNull(message = "Cantidad is required")
    private Integer cantidad;

    private String motivo;
}
