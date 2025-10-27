package com.pagodirecto.ventas.infrastructure.repository;

import com.pagodirecto.ventas.domain.Pedido;
import com.pagodirecto.ventas.domain.PedidoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository: Pedido
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    /**
     * Busca pedidos por cliente
     */
    Page<Pedido> findByClienteId(UUID clienteId, Pageable pageable);

    /**
     * Busca pedidos por status
     */
    Page<Pedido> findByStatus(PedidoStatus status, Pageable pageable);

    /**
     * Busca un pedido por número
     */
    Optional<Pedido> findByNumero(String numero);

    /**
     * Verifica si existe un pedido con el número
     */
    boolean existsByNumero(String numero);

    /**
     * Busca pedidos por rango de fechas
     */
    @Query("SELECT p FROM Pedido p WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin")
    Page<Pedido> findByFechaRange(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        Pageable pageable
    );

    /**
     * Busca pedidos por propietario (vendedor)
     */
    Page<Pedido> findByPropietarioId(UUID propietarioId, Pageable pageable);

    /**
     * Busca pedidos originados de una cotización
     */
    Page<Pedido> findByCotizacionId(UUID cotizacionId, Pageable pageable);

    /**
     * Búsqueda de texto completo
     */
    @Query("SELECT p FROM Pedido p WHERE " +
           "LOWER(p.numero) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.notas) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Pedido> searchByText(@Param("query") String query, Pageable pageable);

    /**
     * Cuenta pedidos por status
     */
    long countByStatus(PedidoStatus status);

    /**
     * Calcula suma total de ventas por rango de fechas
     */
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "AND p.status NOT IN ('CANCELADO', 'DEVUELTO')")
    java.math.BigDecimal sumTotalByFechaRange(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Top clientes por volumen de ventas
     */
    @Query("SELECT p.clienteId, SUM(p.total) as total FROM Pedido p " +
           "WHERE p.status NOT IN ('CANCELADO', 'DEVUELTO') " +
           "GROUP BY p.clienteId ORDER BY total DESC")
    List<Object[]> findTopClientesByVolumen(Pageable pageable);
}
