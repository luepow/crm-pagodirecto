package com.pagodirecto.ventas.application.mapper;

import com.pagodirecto.ventas.application.dto.PedidoDTO;
import com.pagodirecto.ventas.domain.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper: Pedido <-> PedidoDTO
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Mapper(componentModel = "spring")
public interface PedidoMapper {

    /**
     * Convierte entidad Pedido a DTO
     */
    @Mapping(target = "clienteNombre", ignore = true)
    @Mapping(target = "propietarioNombre", ignore = true)
    @Mapping(target = "createdByNombre", ignore = true)
    @Mapping(target = "cantidadItems", expression = "java(pedido.getItems().size())")
    PedidoDTO toDTO(Pedido pedido);

    /**
     * Convierte DTO a entidad Pedido
     */
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Pedido toEntity(PedidoDTO pedidoDTO);

    /**
     * Actualiza una entidad existente con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(PedidoDTO pedidoDTO, @MappingTarget Pedido pedido);
}
