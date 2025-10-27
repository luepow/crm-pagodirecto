package com.pagodirecto.clientes.infrastructure.repository;

import com.pagodirecto.clientes.domain.Cliente;
import com.pagodirecto.clientes.domain.ClienteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    /**
     * Busca un cliente por código
     *
     * @param codigo código único del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByCodigo(String codigo);

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
     * Busca clientes por unidad de negocio
     *
     * @param unidadNegocioId UUID de la unidad de negocio
     * @param pageable paginación
     * @return página de clientes
     */
    Page<Cliente> findByUnidadNegocioId(UUID unidadNegocioId, Pageable pageable);

    /**
     * Busca clientes por status
     *
     * @param status status del cliente
     * @param pageable paginación
     * @return página de clientes
     */
    Page<Cliente> findByStatus(ClienteStatus status, Pageable pageable);

    /**
     * Busca clientes por propietario
     *
     * @param propietarioId UUID del usuario propietario
     * @param pageable paginación
     * @return página de clientes
     */
    Page<Cliente> findByPropietarioId(UUID propietarioId, Pageable pageable);

    /**
     * Búsqueda de clientes por texto (nombre, email, RFC, razón social)
     *
     * @param searchTerm término de búsqueda
     * @param pageable paginación
     * @return página de clientes
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.rfc) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Cliente> searchClientes(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Busca clientes por segmento
     *
     * @param segmento segmento del cliente
     * @param pageable paginación
     * @return página de clientes
     */
    Page<Cliente> findBySegmento(String segmento, Pageable pageable);

    /**
     * Busca clientes con contactos (JOIN FETCH para prevenir N+1)
     *
     * @param id UUID del cliente
     * @return Optional con el cliente y sus contactos
     */
    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.contactos WHERE c.id = :id")
    Optional<Cliente> findByIdWithContactos(@Param("id") UUID id);

    /**
     * Busca clientes con direcciones (JOIN FETCH para prevenir N+1)
     *
     * @param id UUID del cliente
     * @return Optional con el cliente y sus direcciones
     */
    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.direcciones WHERE c.id = :id")
    Optional<Cliente> findByIdWithDirecciones(@Param("id") UUID id);

    /**
     * Busca clientes con contactos y direcciones (JOIN FETCH para prevenir N+1)
     *
     * @param id UUID del cliente
     * @return Optional con el cliente completo
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "LEFT JOIN FETCH c.contactos " +
           "LEFT JOIN FETCH c.direcciones " +
           "WHERE c.id = :id")
    Optional<Cliente> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Cuenta clientes por status
     *
     * @param status status del cliente
     * @return cantidad de clientes
     */
    long countByStatus(ClienteStatus status);

    /**
     * Verifica si existe un cliente con el código dado
     *
     * @param codigo código del cliente
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica si existe un cliente con el email dado
     *
     * @param email email del cliente
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un cliente con el nombre y unidad de negocio dados
     *
     * @param nombre nombre del cliente
     * @param unidadNegocioId UUID de la unidad de negocio
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreAndUnidadNegocioId(String nombre, UUID unidadNegocioId);
}
