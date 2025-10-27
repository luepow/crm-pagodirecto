package com.pagodirecto.clientes.infrastructure.repository;

import com.pagodirecto.clientes.domain.Direccion;
import com.pagodirecto.clientes.domain.DireccionTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio: Dirección
 *
 * Proporciona acceso a datos para la entidad Direccion.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface DireccionRepository extends JpaRepository<Direccion, UUID> {

    /**
     * Busca todas las direcciones de un cliente
     *
     * @param clienteId UUID del cliente
     * @return lista de direcciones
     */
    @Query("SELECT d FROM Direccion d WHERE d.cliente.id = :clienteId")
    List<Direccion> findByClienteId(@Param("clienteId") UUID clienteId);

    /**
     * Busca direcciones por tipo y cliente
     *
     * @param clienteId UUID del cliente
     * @param tipo tipo de dirección
     * @return lista de direcciones
     */
    @Query("SELECT d FROM Direccion d WHERE d.cliente.id = :clienteId AND d.tipo = :tipo")
    List<Direccion> findByClienteIdAndTipo(@Param("clienteId") UUID clienteId, @Param("tipo") DireccionTipo tipo);

    /**
     * Busca la dirección predeterminada de un cliente por tipo
     *
     * @param clienteId UUID del cliente
     * @param tipo tipo de dirección
     * @return Optional con la dirección predeterminada si existe
     */
    @Query("SELECT d FROM Direccion d WHERE d.cliente.id = :clienteId AND d.tipo = :tipo AND d.isDefault = true")
    Optional<Direccion> findDefaultDireccionByClienteIdAndTipo(@Param("clienteId") UUID clienteId, @Param("tipo") DireccionTipo tipo);

    /**
     * Cuenta direcciones de un cliente
     *
     * @param clienteId UUID del cliente
     * @return cantidad de direcciones
     */
    @Query("SELECT COUNT(d) FROM Direccion d WHERE d.cliente.id = :clienteId")
    long countByClienteId(@Param("clienteId") UUID clienteId);
}
