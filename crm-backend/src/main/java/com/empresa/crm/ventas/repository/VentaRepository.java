package com.empresa.crm.ventas.repository;

import com.empresa.crm.ventas.model.EstadoVenta;
import com.empresa.crm.ventas.model.Venta;
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
 * Repository for Venta entity
 * Bounded context: Ventas
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Find venta by folio (unique identifier)
     */
    Optional<Venta> findByFolioAndDeletedAtIsNull(String folio);

    /**
     * Find all non-deleted ventas
     */
    Page<Venta> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Find venta by ID (non-deleted)
     */
    Optional<Venta> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Find ventas by cliente
     */
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId AND v.deletedAt IS NULL")
    Page<Venta> findByClienteId(@Param("clienteId") Long clienteId, Pageable pageable);

    /**
     * Find ventas by estado
     */
    Page<Venta> findByEstadoAndDeletedAtIsNull(EstadoVenta estado, Pageable pageable);

    /**
     * Find ventas by fecha range
     */
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :startDate AND :endDate AND v.deletedAt IS NULL")
    Page<Venta> findByFechaRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    /**
     * Search ventas by folio, cliente name, or estado
     */
    @Query("SELECT v FROM Venta v LEFT JOIN v.cliente c WHERE " +
           "(LOWER(v.folio) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(v.estado AS string)) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "v.deletedAt IS NULL")
    Page<Venta> searchVentas(@Param("search") String search, Pageable pageable);

    /**
     * Check if folio exists
     */
    boolean existsByFolioAndDeletedAtIsNull(String folio);

    /**
     * Find ventas by cliente and estado
     */
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId AND v.estado = :estado AND v.deletedAt IS NULL")
    List<Venta> findByClienteIdAndEstado(
        @Param("clienteId") Long clienteId,
        @Param("estado") EstadoVenta estado
    );

    /**
     * Get total sales amount by periodo
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE " +
           "v.fechaVenta BETWEEN :startDate AND :endDate AND " +
           "v.estado IN ('CONFIRMADA', 'ENVIADA', 'COMPLETADA') AND " +
           "v.deletedAt IS NULL")
    BigDecimal getTotalSalesByPeriodo(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count ventas by estado
     */
    long countByEstadoAndDeletedAtIsNull(EstadoVenta estado);
}
