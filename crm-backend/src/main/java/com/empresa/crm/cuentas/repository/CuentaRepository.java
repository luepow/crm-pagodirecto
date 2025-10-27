package com.empresa.crm.cuentas.repository;

import com.empresa.crm.cuentas.model.Cuenta;
import com.empresa.crm.cuentas.model.EstadoCuenta;
import com.empresa.crm.cuentas.model.TipoCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Cuenta entity
 * Bounded context: Cuentas
 */
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    /**
     * Find cuenta by folio (unique identifier)
     */
    Optional<Cuenta> findByFolioAndDeletedAtIsNull(String folio);

    /**
     * Find all non-deleted cuentas
     */
    Page<Cuenta> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Find cuenta by ID (non-deleted)
     */
    Optional<Cuenta> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Find cuentas by tipo
     */
    Page<Cuenta> findByTipoAndDeletedAtIsNull(TipoCuenta tipo, Pageable pageable);

    /**
     * Find cuentas by estado
     */
    Page<Cuenta> findByEstadoAndDeletedAtIsNull(EstadoCuenta estado, Pageable pageable);

    /**
     * Find cuentas by tipo and estado
     */
    Page<Cuenta> findByTipoAndEstadoAndDeletedAtIsNull(
        TipoCuenta tipo,
        EstadoCuenta estado,
        Pageable pageable
    );

    /**
     * Find cuentas by cliente
     */
    @Query("SELECT c FROM Cuenta c WHERE c.cliente.id = :clienteId AND c.deletedAt IS NULL")
    Page<Cuenta> findByClienteId(@Param("clienteId") Long clienteId, Pageable pageable);

    /**
     * Find cuentas by cliente and tipo
     */
    @Query("SELECT c FROM Cuenta c WHERE c.cliente.id = :clienteId AND c.tipo = :tipo AND c.deletedAt IS NULL")
    Page<Cuenta> findByClienteIdAndTipo(
        @Param("clienteId") Long clienteId,
        @Param("tipo") TipoCuenta tipo,
        Pageable pageable
    );

    /**
     * Find cuentas by cliente and estado
     */
    @Query("SELECT c FROM Cuenta c WHERE c.cliente.id = :clienteId AND c.estado = :estado AND c.deletedAt IS NULL")
    List<Cuenta> findByClienteIdAndEstado(
        @Param("clienteId") Long clienteId,
        @Param("estado") EstadoCuenta estado
    );

    /**
     * Find cuentas vencidas (past due date and PENDIENTE)
     */
    @Query("SELECT c FROM Cuenta c WHERE " +
           "c.estado = 'PENDIENTE' AND " +
           "c.fechaVencimiento < :today AND " +
           "c.deletedAt IS NULL")
    Page<Cuenta> findCuentasVencidas(@Param("today") LocalDate today, Pageable pageable);

    /**
     * Find cuentas vencidas by tipo
     */
    @Query("SELECT c FROM Cuenta c WHERE " +
           "c.tipo = :tipo AND " +
           "c.estado = 'PENDIENTE' AND " +
           "c.fechaVencimiento < :today AND " +
           "c.deletedAt IS NULL")
    Page<Cuenta> findCuentasVencidasByTipo(
        @Param("tipo") TipoCuenta tipo,
        @Param("today") LocalDate today,
        Pageable pageable
    );

    /**
     * Find cuentas by referencia
     */
    @Query("SELECT c FROM Cuenta c WHERE " +
           "c.referenciaTipo = :tipo AND " +
           "c.referenciaId = :id AND " +
           "c.deletedAt IS NULL")
    List<Cuenta> findByReferencia(
        @Param("tipo") String referenciaTipo,
        @Param("id") Long referenciaId
    );

    /**
     * Search cuentas by folio, cliente name, or descripcion
     */
    @Query("SELECT c FROM Cuenta c LEFT JOIN c.cliente cl WHERE " +
           "(LOWER(c.folio) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cl.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "c.deletedAt IS NULL")
    Page<Cuenta> searchCuentas(@Param("search") String search, Pageable pageable);

    /**
     * Check if folio exists
     */
    boolean existsByFolioAndDeletedAtIsNull(String folio);

    /**
     * Get total saldo by tipo
     */
    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM Cuenta c WHERE " +
           "c.tipo = :tipo AND " +
           "c.estado IN ('PENDIENTE', 'VENCIDO') AND " +
           "c.deletedAt IS NULL")
    BigDecimal getTotalSaldoByTipo(@Param("tipo") TipoCuenta tipo);

    /**
     * Get total saldo by cliente
     */
    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM Cuenta c WHERE " +
           "c.cliente.id = :clienteId AND " +
           "c.estado IN ('PENDIENTE', 'VENCIDO') AND " +
           "c.deletedAt IS NULL")
    BigDecimal getTotalSaldoByCliente(@Param("clienteId") Long clienteId);

    /**
     * Count cuentas by estado
     */
    long countByEstadoAndDeletedAtIsNull(EstadoCuenta estado);

    /**
     * Count cuentas vencidas
     */
    @Query("SELECT COUNT(c) FROM Cuenta c WHERE " +
           "c.estado = 'PENDIENTE' AND " +
           "c.fechaVencimiento < :today AND " +
           "c.deletedAt IS NULL")
    long countCuentasVencidas(@Param("today") LocalDate today);
}
