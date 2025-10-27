package com.empresa.crm.productos.infrastructure.repository;

import com.empresa.crm.productos.domain.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CategoriaProducto entity.
 * Infrastructure layer - Hexagonal architecture
 */
@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, UUID> {

    /**
     * Find all active categories (not soft deleted)
     */
    @Query("SELECT c FROM CategoriaProducto c WHERE c.deletedAt IS NULL AND c.activo = true ORDER BY c.nombre")
    List<CategoriaProducto> findAllActive();

    /**
     * Find category by ID excluding soft deleted
     */
    @Query("SELECT c FROM CategoriaProducto c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<CategoriaProducto> findByIdAndNotDeleted(UUID id);

    /**
     * Find category by name
     */
    @Query("SELECT c FROM CategoriaProducto c WHERE c.nombre = :nombre AND c.deletedAt IS NULL")
    Optional<CategoriaProducto> findByNombre(String nombre);

    /**
     * Check if category exists by name (excluding current category)
     */
    @Query("SELECT COUNT(c) > 0 FROM CategoriaProducto c WHERE c.nombre = :nombre AND c.id != :id AND c.deletedAt IS NULL")
    boolean existsByNombreAndIdNot(String nombre, UUID id);

    /**
     * Check if category exists by name
     */
    @Query("SELECT COUNT(c) > 0 FROM CategoriaProducto c WHERE c.nombre = :nombre AND c.deletedAt IS NULL")
    boolean existsByNombre(String nombre);
}
