package com.empresa.crm.productos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity for product categories.
 * Bounded context: Productos
 */
@Entity
@Table(name = "productos_categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Soft delete implementation
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.activo = false;
    }

    /**
     * Check if category is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
