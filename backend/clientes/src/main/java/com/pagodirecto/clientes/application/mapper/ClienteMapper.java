package com.pagodirecto.clientes.application.mapper;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import com.pagodirecto.clientes.application.dto.ContactoDTO;
import com.pagodirecto.clientes.application.dto.DireccionDTO;
import com.pagodirecto.clientes.domain.Cliente;
import com.pagodirecto.clientes.domain.Contacto;
import com.pagodirecto.clientes.domain.Direccion;
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
    @Mapping(target = "contactos", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
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
    @Mapping(target = "contactos", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(ClienteDTO clienteDTO, @MappingTarget Cliente cliente);

    /**
     * Convierte entidad Contacto a DTO
     *
     * @param contacto entidad
     * @return DTO
     */
    @Mapping(target = "clienteId", source = "cliente.id")
    ContactoDTO toContactoDTO(Contacto contacto);

    /**
     * Convierte DTO a entidad Contacto
     *
     * @param contactoDTO DTO
     * @return entidad
     */
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Contacto toContactoEntity(ContactoDTO contactoDTO);

    /**
     * Convierte lista de entidades Contacto a lista de DTOs
     *
     * @param contactos lista de entidades
     * @return lista de DTOs
     */
    List<ContactoDTO> toContactoDTOList(List<Contacto> contactos);

    /**
     * Convierte entidad Direccion a DTO
     *
     * @param direccion entidad
     * @return DTO
     */
    @Mapping(target = "clienteId", source = "cliente.id")
    DireccionDTO toDireccionDTO(Direccion direccion);

    /**
     * Convierte DTO a entidad Direccion
     *
     * @param direccionDTO DTO
     * @return entidad
     */
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Direccion toDireccionEntity(DireccionDTO direccionDTO);

    /**
     * Convierte lista de entidades Direccion a lista de DTOs
     *
     * @param direcciones lista de entidades
     * @return lista de DTOs
     */
    List<DireccionDTO> toDireccionDTOList(List<Direccion> direcciones);
}
