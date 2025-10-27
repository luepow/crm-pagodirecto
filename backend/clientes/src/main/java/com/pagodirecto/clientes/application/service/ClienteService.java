package com.pagodirecto.clientes.application.service;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * @param usuarioId ID del usuario que crea el registro
     * @return DTO del cliente creado
     */
    ClienteDTO crear(ClienteDTO clienteDTO, Long usuarioId);

    /**
     * Actualiza un cliente existente
     *
     * @param id ID del cliente
     * @param clienteDTO datos actualizados
     * @param usuarioId ID del usuario que actualiza
     * @return DTO del cliente actualizado
     */
    ClienteDTO actualizar(Long id, ClienteDTO clienteDTO, Long usuarioId);

    /**
     * Busca un cliente por ID
     *
     * @param id ID del cliente
     * @return DTO del cliente
     */
    ClienteDTO buscarPorId(Long id);

    /**
     * Lista todos los clientes con paginación
     *
     * @param pageable configuración de paginación
     * @return página de clientes
     */
    Page<ClienteDTO> listarTodos(Pageable pageable);

    /**
     * Busca clientes por estado activo
     *
     * @param activo estado activo del cliente
     * @param pageable configuración de paginación
     * @return página de clientes
     */
    Page<ClienteDTO> buscarPorActivo(Boolean activo, Pageable pageable);

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
     * @param id ID del cliente
     */
    void eliminar(Long id);

    /**
     * Activa un cliente
     *
     * @param id ID del cliente
     * @param usuarioId ID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO activar(Long id, Long usuarioId);

    /**
     * Desactiva un cliente
     *
     * @param id ID del cliente
     * @param usuarioId ID del usuario que realiza la acción
     * @return DTO del cliente actualizado
     */
    ClienteDTO desactivar(Long id, Long usuarioId);
}
