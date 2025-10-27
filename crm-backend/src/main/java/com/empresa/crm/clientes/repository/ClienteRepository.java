package com.empresa.crm.clientes.repository;

import com.empresa.crm.clientes.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Find all non-deleted clientes with pagination
     */
    Page<Cliente> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Find a cliente by ID that is not deleted
     */
    Optional<Cliente> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Find clientes by name (case-insensitive, partial match) that are not deleted
     */
    Page<Cliente> findByNombreContainingIgnoreCaseAndDeletedAtIsNull(String nombre, Pageable pageable);

    /**
     * Search clientes by multiple criteria (nombre, email, RFC) with pagination
     */
    @Query("SELECT c FROM Cliente c WHERE c.deletedAt IS NULL " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.rfc) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Cliente> searchClientes(@Param("search") String search, Pageable pageable);

    /**
     * Check if email exists (excluding deleted records)
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Check if email exists for another cliente (used for updates)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Cliente c WHERE c.email = :email AND c.id <> :id AND c.deletedAt IS NULL")
    boolean existsByEmailAndIdNotAndDeletedAtIsNull(@Param("email") String email, @Param("id") Long id);

    /**
     * Find clientes by activo status and not deleted
     */
    Page<Cliente> findByActivoAndDeletedAtIsNull(Boolean activo, Pageable pageable);
}
