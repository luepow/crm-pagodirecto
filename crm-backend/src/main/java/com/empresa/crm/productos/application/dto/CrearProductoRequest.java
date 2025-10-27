package com.empresa.crm.productos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a product.
 * Application layer - Input validation aligned with V3 migration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearProductoRequest {

    @NotBlank(message = "Codigo is required")
    @Size(min = 1, max = 50, message = "Codigo must be between 1 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Codigo must contain only uppercase letters, numbers, and hyphens")
    private String codigo;

    @NotBlank(message = "Nombre is required")
    @Size(min = 2, max = 200, message = "Nombre must be between 2 and 200 characters")
    private String nombre;

    @Size(max = 1000, message = "Descripcion must not exceed 1000 characters")
    private String descripcion;

    @NotNull(message = "Categoria ID is required")
    private UUID categoriaId;

    @NotNull(message = "Precio is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Precio must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Precio must have at most 10 integer digits and 2 decimal places")
    private BigDecimal precio;

    @NotNull(message = "Costo is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Costo must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Costo must have at most 10 integer digits and 2 decimal places")
    private BigDecimal costo;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Stock minimo is required")
    @Min(value = 0, message = "Stock minimo cannot be negative")
    private Integer stockMinimo;

    @NotBlank(message = "Unidad medida is required")
    @Size(max = 20, message = "Unidad medida must not exceed 20 characters")
    private String unidadMedida;

    @Builder.Default
    private Boolean activo = true;

    /**
     * Business validation: Precio should be >= Costo
     */
    @AssertTrue(message = "Precio must be greater than or equal to Costo")
    private boolean isPrecioValid() {
        if (precio == null || costo == null) {
            return true; // Let @NotNull handle null validation
        }
        return precio.compareTo(costo) >= 0;
    }
}
