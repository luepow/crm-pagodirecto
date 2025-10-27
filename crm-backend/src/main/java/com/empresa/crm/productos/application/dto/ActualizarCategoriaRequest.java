package com.empresa.crm.productos.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a product category.
 * Application layer - Input validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarCategoriaRequest {

    @Size(min = 2, max = 100, message = "Nombre must be between 2 and 100 characters")
    private String nombre;

    @Size(max = 500, message = "Descripcion must not exceed 500 characters")
    private String descripcion;

    private Boolean activo;
}
