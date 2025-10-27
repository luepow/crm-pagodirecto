package com.empresa.crm.productos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for updating a product.
 * Application layer - Input validation
 * All fields optional to support partial updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarProductoRequest {

    @Size(min = 1, max = 50, message = "Codigo must be between 1 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Codigo must contain only uppercase letters, numbers, and hyphens")
    private String codigo;

    @Size(min = 2, max = 200, message = "Nombre must be between 2 and 200 characters")
    private String nombre;

    @Size(max = 1000, message = "Descripcion must not exceed 1000 characters")
    private String descripcion;

    private UUID categoriaId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Precio must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Precio must have at most 10 integer digits and 2 decimal places")
    private BigDecimal precio;

    @DecimalMin(value = "0.0", inclusive = false, message = "Costo must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Costo must have at most 10 integer digits and 2 decimal places")
    private BigDecimal costo;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Min(value = 0, message = "Stock minimo cannot be negative")
    private Integer stockMinimo;

    @Size(max = 20, message = "Unidad medida must not exceed 20 characters")
    private String unidadMedida;

    private Boolean activo;

    /**
     * Business validation: Precio should be >= Costo (only when both are provided)
     */
    @AssertTrue(message = "Precio must be greater than or equal to Costo")
    private boolean isPrecioValid() {
        if (precio == null || costo == null) {
            return true;
        }
        return precio.compareTo(costo) >= 0;
    }
}
