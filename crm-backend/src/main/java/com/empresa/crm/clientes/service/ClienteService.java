package com.empresa.crm.clientes.service;

import com.empresa.crm.clientes.dto.ActualizarClienteRequest;
import com.empresa.crm.clientes.dto.ClienteDTO;
import com.empresa.crm.clientes.dto.CrearClienteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ClienteService {

    /**
     * List all clientes with optional search and pagination
     * @param search Optional search term (searches in nombre, email, rfc)
     * @param pageable Pagination parameters
     * @return Page of ClienteDTO
     */
    Page<ClienteDTO> listar(String search, Pageable pageable);

    /**
     * Get a single cliente by ID
     * @param id Cliente ID
     * @return Optional ClienteDTO
     */
    Optional<ClienteDTO> obtener(Long id);

    /**
     * Create a new cliente
     * @param request Cliente creation request
     * @return Created ClienteDTO
     */
    ClienteDTO crear(CrearClienteRequest request);

    /**
     * Update an existing cliente
     * @param id Cliente ID
     * @param request Cliente update request
     * @return Optional updated ClienteDTO
     */
    Optional<ClienteDTO> actualizar(Long id, ActualizarClienteRequest request);

    /**
     * Soft delete a cliente
     * @param id Cliente ID
     */
    void eliminar(Long id);

    /**
     * Check if email is already in use
     * @param email Email to check
     * @return true if email exists
     */
    boolean existeEmail(String email);

    /**
     * Check if email is in use by another cliente (for updates)
     * @param email Email to check
     * @param id Cliente ID to exclude
     * @return true if email exists for another cliente
     */
    boolean existeEmailParaOtro(String email, Long id);
}
