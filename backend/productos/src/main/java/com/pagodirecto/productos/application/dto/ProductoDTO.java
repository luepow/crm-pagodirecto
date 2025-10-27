package com.pagodirecto.productos.application.dto;

import com.pagodirecto.productos.domain.ProductoStatus;
import com.pagodirecto.productos.domain.ProductoTipo;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO para transferencia de datos de Productos
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private UUID id;

    @NotNull(message = "La unidad de negocio es obligatoria")
    private UUID unidadNegocioId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 100, message = "El código no puede exceder 100 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String descripcion;

    private UUID categoriaId;

    private String categoriaNombre;

    @NotNull(message = "El tipo de producto es obligatorio")
    private ProductoTipo tipo;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "Formato de precio inválido")
    private BigDecimal precioBase;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres (ISO 4217)")
    private String moneda;

    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "Formato de costo inválido")
    private BigDecimal costoUnitario;

    @NotNull(message = "El status es obligatorio")
    private ProductoStatus status;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockActual;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    private String unidadMedida;

    @DecimalMin(value = "0.0", message = "El peso no puede ser negativo")
    @Digits(integer = 7, fraction = 3, message = "Formato de peso inválido")
    private BigDecimal pesoKg;

    @Size(max = 100, message = "El SKU no puede exceder 100 caracteres")
    private String sku;

    @Size(max = 100, message = "El código de barras no puede exceder 100 caracteres")
    private String codigoBarras;

    private String imagenUrl;

    private Boolean requiereReabastecimiento;

    private BigDecimal margenBruto;

    private Instant createdAt;

    private UUID createdBy;

    private String createdByNombre;

    private Instant updatedAt;

    private UUID updatedBy;
}
