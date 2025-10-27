package com.empresa.crm.productos.application.dto;

import com.empresa.crm.productos.domain.CategoriaProducto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for CategoriaProducto entity.
 * Application layer - Clean architecture
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProductoDTO {

    private UUID id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method: Convert entity to DTO
     */
    public static CategoriaProductoDTO fromEntity(CategoriaProducto entity) {
        if (entity == null) {
            return null;
        }
        return CategoriaProductoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert DTO to entity (for creation)
     */
    public CategoriaProducto toEntity() {
        return CategoriaProducto.builder()
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .activo(this.activo != null ? this.activo : true)
                .build();
    }
}
