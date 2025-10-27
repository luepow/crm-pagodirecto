package com.pagodirecto.clientes.infrastructure.repository;

import com.pagodirecto.clientes.domain.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio: Contacto
 *
 * Proporciona acceso a datos para la entidad Contacto.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface ContactoRepository extends JpaRepository<Contacto, UUID> {

    /**
     * Busca todos los contactos de un cliente
     *
     * @param clienteId UUID del cliente
     * @return lista de contactos
     */
    @Query("SELECT c FROM Contacto c WHERE c.cliente.id = :clienteId")
    List<Contacto> findByClienteId(@Param("clienteId") UUID clienteId);

    /**
     * Busca el contacto principal de un cliente
     *
     * @param clienteId UUID del cliente
     * @return Optional con el contacto principal si existe
     */
    @Query("SELECT c FROM Contacto c WHERE c.cliente.id = :clienteId AND c.isPrimary = true")
    Optional<Contacto> findPrimaryContactoByClienteId(@Param("clienteId") UUID clienteId);

    /**
     * Busca contactos por email
     *
     * @param email email del contacto
     * @return lista de contactos con ese email
     */
    List<Contacto> findByEmail(String email);

    /**
     * Cuenta contactos de un cliente
     *
     * @param clienteId UUID del cliente
     * @return cantidad de contactos
     */
    @Query("SELECT COUNT(c) FROM Contacto c WHERE c.cliente.id = :clienteId")
    long countByClienteId(@Param("clienteId") UUID clienteId);
}
