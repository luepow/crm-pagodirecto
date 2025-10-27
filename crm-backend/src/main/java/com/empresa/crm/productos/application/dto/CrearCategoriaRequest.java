package com.empresa.crm.productos.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a product category.
 * Application layer - Input validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearCategoriaRequest {

    @NotBlank(message = "Nombre is required")
    @Size(min = 2, max = 100, message = "Nombre must be between 2 and 100 characters")
    private String nombre;

    @Size(max = 500, message = "Descripcion must not exceed 500 characters")
    private String descripcion;

    @Builder.Default
    private Boolean activo = true;
}
