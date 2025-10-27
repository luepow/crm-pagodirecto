package com.empresa.crm.productos.application.dto;

import com.empresa.crm.productos.domain.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Producto entity with embedded category.
 * Application layer - Clean architecture
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private CategoriaProductoDTO categoria;
    private BigDecimal precio;
    private BigDecimal costo;
    private Integer stock;
    private Integer stockMinimo;
    private String unidadMedida;
    private Boolean activo;
    private Boolean stockBajo;
    private BigDecimal margenBeneficio;

    // Audit fields
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    /**
     * Factory method: Convert entity to DTO
     */
    public static ProductoDTO fromEntity(Producto entity) {
        if (entity == null) {
            return null;
        }
        return ProductoDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .categoria(CategoriaProductoDTO.fromEntity(entity.getCategoria()))
                .precio(entity.getPrecio())
                .costo(entity.getCosto())
                .stock(entity.getStock())
                .stockMinimo(entity.getStockMinimo())
                .unidadMedida(entity.getUnidadMedida())
                .activo(entity.getActivo())
                .stockBajo(entity.hasLowStock())
                .margenBeneficio(entity.calculateProfitMargin())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
