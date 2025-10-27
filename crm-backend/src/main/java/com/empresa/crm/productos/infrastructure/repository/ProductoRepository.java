package com.empresa.crm.productos.infrastructure.repository;

import com.empresa.crm.productos.domain.Producto;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Producto entity.
 * Infrastructure layer - Hexagonal architecture
 * Custom queries aligned with V3 migration schema
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    /**
     * Find all active products (not soft deleted)
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE p.deletedAt IS NULL ORDER BY p.nombre")
    List<Producto> findAllActive();

    /**
     * Find product by ID excluding soft deleted, with category eagerly loaded
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Producto> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Find product by codigo (unique identifier)
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE p.codigo = :codigo AND p.deletedAt IS NULL")
    Optional<Producto> findByCodigo(@Param("codigo") String codigo);

    /**
     * Search products by name (case-insensitive, partial match)
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.deletedAt IS NULL ORDER BY p.nombre")
    List<Producto> findByNombreContaining(@Param("nombre") String nombre);

    /**
     * Find products by category ID
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria c WHERE c.id = :categoriaId AND p.deletedAt IS NULL ORDER BY p.nombre")
    List<Producto> findByCategoriaId(@Param("categoriaId") UUID categoriaId);

    /**
     * Find products with low stock (stock <= stock_minimo)
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE p.stock <= p.stockMinimo AND p.activo = true AND p.deletedAt IS NULL ORDER BY p.stock ASC, p.nombre")
    List<Producto> findProductosConStockBajo();

    /**
     * Find active products by category
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria c WHERE c.id = :categoriaId AND p.activo = true AND p.deletedAt IS NULL ORDER BY p.nombre")
    List<Producto> findActivosByCategoriaId(@Param("categoriaId") UUID categoriaId);

    /**
     * Check if product code exists (excluding current product)
     */
    @Query("SELECT COUNT(p) > 0 FROM Producto p WHERE p.codigo = :codigo AND p.id != :id AND p.deletedAt IS NULL")
    boolean existsByCodigoAndIdNot(@Param("codigo") String codigo, @Param("id") UUID id);

    /**
     * Check if product code exists
     */
    @Query("SELECT COUNT(p) > 0 FROM Producto p WHERE p.codigo = :codigo AND p.deletedAt IS NULL")
    boolean existsByCodigo(@Param("codigo") String codigo);

    /**
     * Search products with multiple filters
     * @param nombre partial name match (optional)
     * @param categoriaId category filter (optional)
     * @param activo active status filter (optional)
     */
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.categoria c WHERE " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:categoriaId IS NULL OR c.id = :categoriaId) AND " +
           "(:activo IS NULL OR p.activo = :activo) AND " +
           "p.deletedAt IS NULL ORDER BY p.nombre")
    List<Producto> searchProductos(
        @Param("nombre") String nombre,
        @Param("categoriaId") UUID categoriaId,
        @Param("activo") Boolean activo
    );

    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.deletedAt IS NULL")
    long countByCategoriaId(@Param("categoriaId") UUID categoriaId);
}
