package com.empresa.crm.ventas.repository;

import com.empresa.crm.ventas.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for DetalleVenta entity
 * Bounded context: Ventas
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Find detalles by venta ID
     */
    @Query("SELECT d FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    List<DetalleVenta> findByVentaId(@Param("ventaId") Long ventaId);

    /**
     * Find detalles by multiple venta IDs
     */
    @Query("SELECT d FROM DetalleVenta d WHERE d.venta.id IN :ventaIds")
    List<DetalleVenta> findByVentaIdIn(@Param("ventaIds") List<Long> ventaIds);

    /**
     * Find detalles by producto ID
     */
    @Query("SELECT d FROM DetalleVenta d WHERE d.producto.id = :productoId")
    List<DetalleVenta> findByProductoId(@Param("productoId") UUID productoId);

    /**
     * Count productos vendidos
     */
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleVenta d WHERE d.producto.id = :productoId")
    Integer countProductoVendido(@Param("productoId") UUID productoId);
}
