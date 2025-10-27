package com.empresa.crm.ventas.model;

import com.empresa.crm.productos.domain.Producto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity for sale details/line items.
 * Bounded context: Ventas
 * Aligned with V4 migration schema
 */
@Entity
@Table(name = "detalles_venta", indexes = {
    @Index(name = "idx_detalle_venta", columnList = "venta_id"),
    @Index(name = "idx_detalle_producto", columnList = "producto_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateSubtotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateSubtotal();
    }

    /**
     * Business logic: Calculate subtotal
     */
    public void calculateSubtotal() {
        if (cantidad == null || precioUnitario == null) {
            this.subtotal = BigDecimal.ZERO;
            return;
        }

        BigDecimal subtotalBeforeDiscount = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        this.subtotal = subtotalBeforeDiscount.subtract(
            descuento != null ? descuento : BigDecimal.ZERO
        );
    }

    /**
     * Validate detalle invariants
     */
    public void validate() {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad must be positive");
        }
        if (precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Precio unitario must be positive");
        }
        if (descuento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Descuento cannot be negative");
        }
        if (descuento.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Descuento cannot exceed subtotal");
        }
    }
}
