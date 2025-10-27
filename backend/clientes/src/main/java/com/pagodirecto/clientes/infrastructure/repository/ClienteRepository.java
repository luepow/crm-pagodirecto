package com.pagodirecto.clientes.infrastructure.repository;

import com.pagodirecto.clientes.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio: Cliente
 *
 * Proporciona acceso a datos para la entidad Cliente con queries optimizadas.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por email
     *
     * @param email email del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca un cliente por RFC
     *
     * @param rfc RFC del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByRfc(String rfc);

    /**
     * Busca clientes activos
     *
     * @param activo estado activo del cliente
     * @param pageable paginación
     * @return página de clientes
     */
    Page<Cliente> findByActivo(Boolean activo, Pageable pageable);

    /**
     * Búsqueda de clientes por texto (nombre, email, RFC)
     *
     * @param searchTerm término de búsqueda
     * @param pageable paginación
     * @return página de clientes
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.rfc) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Cliente> searchClientes(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Verifica si existe un cliente con el email dado
     *
     * @param email email del cliente
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
