package com.pagodirecto.productos.application.mapper;

import com.pagodirecto.productos.application.dto.ProductoDTO;
import com.pagodirecto.productos.domain.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper: Producto <-> ProductoDTO
 *
 * Mapper MapStruct para conversión entre entidad Producto y DTO.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Mapper(componentModel = "spring")
public interface ProductoMapper {

    /**
     * Convierte entidad Producto a DTO
     */
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "requiereReabastecimiento", expression = "java(producto.requiereReabastecimiento())")
    @Mapping(target = "margenBruto", expression = "java(producto.calcularMargenBruto())")
    @Mapping(target = "createdByNombre", ignore = true)
    ProductoDTO toDTO(Producto producto);

    /**
     * Convierte DTO a entidad Producto
     */
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "precios", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Producto toEntity(ProductoDTO productoDTO);

    /**
     * Actualiza una entidad existente con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "precios", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(ProductoDTO productoDTO, @MappingTarget Producto producto);
}
