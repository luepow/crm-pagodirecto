package com.pagodirecto.productos.infrastructure.repository;

import com.pagodirecto.productos.domain.Producto;
import com.pagodirecto.productos.domain.ProductoStatus;
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
 * Repository: Producto
 *
 * Repositorio JPA para operaciones de persistencia de Productos.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    /**
     * Busca productos por status
     */
    Page<Producto> findByStatus(ProductoStatus status, Pageable pageable);

    /**
     * Busca productos por categoría
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId")
    Page<Producto> findByCategoriaId(@Param("categoriaId") UUID categoriaId, Pageable pageable);

    /**
     * Busca un producto por código único
     */
    Optional<Producto> findByCodigo(String codigo);

    /**
     * Busca un producto por SKU
     */
    Optional<Producto> findBySku(String sku);

    /**
     * Verifica si existe un producto con el código
     */
    boolean existsByCodigo(String codigo);

    /**
     * Verifica si existe un producto con el SKU
     */
    boolean existsBySku(String sku);

    /**
     * Busca productos activos
     */
    @Query("SELECT p FROM Producto p WHERE p.status = 'ACTIVE'")
    Page<Producto> findProductosActivos(Pageable pageable);

    /**
     * Busca productos que requieren reabastecimiento
     */
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.status = 'ACTIVE'")
    List<Producto> findProductosParaReabastecer();

    /**
     * Búsqueda de texto completo
     */
    @Query("SELECT p FROM Producto p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Producto> searchByText(@Param("query") String query, Pageable pageable);

    /**
     * Cuenta productos por status
     */
    long countByStatus(ProductoStatus status);

    /**
     * Encuentra productos por unidad de negocio
     */
    Page<Producto> findByUnidadNegocioId(UUID unidadNegocioId, Pageable pageable);
}
