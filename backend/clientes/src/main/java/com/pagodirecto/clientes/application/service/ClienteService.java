package com.pagodirecto.clientes.application.service;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import com.pagodirecto.clientes.domain.Cliente;
import com.pagodirecto.clientes.domain.ClienteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Servicio: Cliente
 *
 * Interface de servicio para operaciones de negocio relacionadas con clientes.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface ClienteService {

    /**
     * Crea un nuevo cliente
     *
     * @param clienteDTO datos del cliente
     * @param usuarioId UUID del usuario que crea el registro
     * @return DTO del cliente creado
     */
    ClienteDTO crear(ClienteDTO clienteDTO, UUID usuarioId);

    /**
     * Actualiza un cliente existente
     *
     * @param id UUID del cliente
     * @param clienteDTO datos actualizados
     * @param usuarioId UUID del usuario que actualiza
     * @return DTO del cliente actualizado
     */
    ClienteDTO actualizar(UUID id, ClienteDTO clienteDTO, UUID usuarioId);

    /**
     * Busca un cliente por ID
     *
     * @param id UUID del cliente
     * @return DTO del cliente
     */
    ClienteDTO buscarPorId(UUID id);

    /**
     * Busca un cliente por código
     *
     * @param codigo código único del cliente
     * @return DTO del cliente
     */
    ClienteDTO buscarPorCodigo(String codigo);

    /**
     * Lista todos los clientes con paginación
     *
     * @param pageable configuración de paginación
     * @return página de clientes
     */
    Page<ClienteDTO> listarTodos(Pageable pageable);

    /**
     * Busca clientes por status
     *
     * @param status status del cliente
     * @param pageable configuración de paginación
     * @return página de clientes
     */
    Page<ClienteDTO> buscarPorStatus(ClienteStatus status, Pageable pageable);

    /**
     * Busca clientes por propietario
     *
     * @param propietarioId UUID del usuario propietario
     * @param pageable configuración de paginación
     * @return página de clientes
     */
    Page<ClienteDTO> buscarPorPropietario(UUID propietarioId, Pageable pageable);

    /**
     * Busca clientes por término de búsqueda
     *
     * @param searchTerm término de búsqueda
     * @param pageable configuración de paginación
     * @return página de clientes
     */
    Page<ClienteDTO> buscar(String searchTerm, Pageable pageable);

    /**
     * Elimina un cliente (soft delete)
     *
     * @param id UUID del cliente
     */
    void eliminar(UUID id);

    /**
     * Activa un cliente
     *
     * @param id UUID del cliente
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO activar(UUID id, UUID usuarioId);

    /**
     * Desactiva un cliente
     *
     * @param id UUID del cliente
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO desactivar(UUID id, UUID usuarioId);

    /**
     * Convierte un lead a prospecto
     *
     * @param id UUID del cliente
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO convertirAProspecto(UUID id, UUID usuarioId);

    /**
     * Convierte un prospecto a cliente activo
     *
     * @param id UUID del cliente
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO convertirACliente(UUID id, UUID usuarioId);

    /**
     * Agrega un cliente a la lista negra
     *
     * @param id UUID del cliente
     * @param motivo motivo del blacklist
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO agregarABlacklist(UUID id, String motivo, UUID usuarioId);

    /**
     * Cuenta clientes por status
     *
     * @param status status del cliente
     * @return cantidad de clientes
     */
    long contarPorStatus(ClienteStatus status);
}
