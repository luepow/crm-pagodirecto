package com.empresa.crm.productos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity for products.
 * Bounded context: Productos
 * Aligned with V3 migration schema
 */
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_codigo", columnList = "codigo"),
    @Index(name = "idx_producto_categoria", columnList = "categoria_id"),
    @Index(name = "idx_producto_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaProducto categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    @Builder.Default
    private Integer stockMinimo = 0;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

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
     * Business logic: Check if product has low stock
     */
    public boolean hasLowStock() {
        return stock != null && stockMinimo != null && stock <= stockMinimo;
    }

    /**
     * Business logic: Calculate profit margin
     */
    public BigDecimal calculateProfitMargin() {
        if (precio == null || costo == null || costo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precio.subtract(costo)
                .divide(precio, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Business logic: Update stock
     */
    public void adjustStock(int quantity) {
        if (this.stock == null) {
            this.stock = 0;
        }
        this.stock += quantity;
        if (this.stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }

    /**
     * Soft delete implementation
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.activo = false;
    }

    /**
     * Check if product is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Validate product invariants
     */
    public void validate() {
        if (precio.compareTo(costo) < 0) {
            throw new IllegalArgumentException("Precio cannot be less than costo");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("Stock minimo cannot be negative");
        }
    }
}
