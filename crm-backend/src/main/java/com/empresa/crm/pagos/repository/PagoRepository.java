package com.empresa.crm.pagos.repository;

import com.empresa.crm.pagos.model.EstadoPago;
import com.empresa.crm.pagos.model.MetodoPago;
import com.empresa.crm.pagos.model.Pago;
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
 * Repository for Pago entity
 * Bounded context: Pagos
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Find pago by folio (unique identifier)
     */
    Optional<Pago> findByFolioAndDeletedAtIsNull(String folio);

    /**
     * Find all non-deleted pagos
     */
    Page<Pago> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Find pago by ID (non-deleted)
     */
    Optional<Pago> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Find pagos by venta
     */
    @Query("SELECT p FROM Pago p WHERE p.venta.id = :ventaId AND p.deletedAt IS NULL")
    List<Pago> findByVentaId(@Param("ventaId") Long ventaId);

    /**
     * Find pagos by venta (paginated)
     */
    @Query("SELECT p FROM Pago p WHERE p.venta.id = :ventaId AND p.deletedAt IS NULL")
    Page<Pago> findByVentaIdPaginated(@Param("ventaId") Long ventaId, Pageable pageable);

    /**
     * Find pagos by estado
     */
    Page<Pago> findByEstadoAndDeletedAtIsNull(EstadoPago estado, Pageable pageable);

    /**
     * Find pagos by metodo pago
     */
    Page<Pago> findByMetodoPagoAndDeletedAtIsNull(MetodoPago metodoPago, Pageable pageable);

    /**
     * Find pagos by fecha range
     */
    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :startDate AND :endDate AND p.deletedAt IS NULL")
    Page<Pago> findByFechaRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    /**
     * Search pagos by folio, referencia, or venta folio
     */
    @Query("SELECT p FROM Pago p LEFT JOIN p.venta v WHERE " +
           "(LOWER(p.folio) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.referencia) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.folio) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "p.deletedAt IS NULL")
    Page<Pago> searchPagos(@Param("search") String search, Pageable pageable);

    /**
     * Check if folio exists
     */
    boolean existsByFolioAndDeletedAtIsNull(String folio);

    /**
     * Get total pagos amount by venta
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE " +
           "p.venta.id = :ventaId AND " +
           "p.estado = 'COMPLETADO' AND " +
           "p.deletedAt IS NULL")
    BigDecimal getTotalPagosByVenta(@Param("ventaId") Long ventaId);

    /**
     * Get total pagos amount by periodo
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE " +
           "p.fechaPago BETWEEN :startDate AND :endDate AND " +
           "p.estado = 'COMPLETADO' AND " +
           "p.deletedAt IS NULL")
    BigDecimal getTotalPagosByPeriodo(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count pagos by estado
     */
    long countByEstadoAndDeletedAtIsNull(EstadoPago estado);

    /**
     * Find pagos pendientes
     */
    @Query("SELECT p FROM Pago p WHERE p.estado = 'PENDIENTE' AND p.deletedAt IS NULL")
    Page<Pago> findPagosPendientes(Pageable pageable);
}
