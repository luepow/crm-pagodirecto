package com.pagodirecto.clientes.application.mapper;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import com.pagodirecto.clientes.domain.Cliente;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper: Cliente
 *
 * MapStruct mapper para convertir entre entidades y DTOs del dominio Cliente.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {

    /**
     * Convierte entidad Cliente a DTO
     *
     * @param cliente entidad
     * @return DTO
     */
    ClienteDTO toDTO(Cliente cliente);

    /**
     * Convierte DTO a entidad Cliente
     *
     * @param clienteDTO DTO
     * @return entidad
     */
    @Mapping(target = "deletedAt", ignore = true)
    Cliente toEntity(ClienteDTO clienteDTO);

    /**
     * Convierte lista de entidades a lista de DTOs
     *
     * @param clientes lista de entidades
     * @return lista de DTOs
     */
    List<ClienteDTO> toDTOList(List<Cliente> clientes);

    /**
     * Actualiza una entidad existente con datos del DTO
     *
     * @param clienteDTO DTO con datos actualizados
     * @param cliente entidad a actualizar
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(ClienteDTO clienteDTO, @MappingTarget Cliente cliente);
}
