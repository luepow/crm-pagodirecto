package com.empresa.crm.ventas.model;

import com.empresa.crm.clientes.model.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity for sales.
 * Bounded context: Ventas
 * Aligned with V4 migration schema
 */
@Entity
@Table(name = "ventas", indexes = {
    @Index(name = "idx_venta_folio", columnList = "folio", unique = true),
    @Index(name = "idx_venta_cliente", columnList = "cliente_id"),
    @Index(name = "idx_venta_fecha", columnList = "fecha_venta"),
    @Index(name = "idx_venta_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.BORRADOR;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal impuestos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVenta> detalles = new ArrayList<>();

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

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
     * Business logic: Calculate totals from detalles
     */
    public void calculateTotals() {
        this.subtotal = detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total = subtotal - descuento + impuestos
        this.total = this.subtotal
                .subtract(this.descuento != null ? this.descuento : BigDecimal.ZERO)
                .add(this.impuestos != null ? this.impuestos : BigDecimal.ZERO);
    }

    /**
     * Business logic: Add detalle to venta
     */
    public void addDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
        calculateTotals();
    }

    /**
     * Business logic: Remove detalle from venta
     */
    public void removeDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
        calculateTotals();
    }

    /**
     * Business logic: Change estado
     */
    public void cambiarEstado(EstadoVenta nuevoEstado) {
        validateEstadoTransition(nuevoEstado);
        this.estado = nuevoEstado;
    }

    /**
     * Validate estado transition rules
     */
    private void validateEstadoTransition(EstadoVenta nuevoEstado) {
        if (this.estado == EstadoVenta.CANCELADA) {
            throw new IllegalStateException("Cannot change estado from CANCELADA");
        }
        if (this.estado == EstadoVenta.COMPLETADA && nuevoEstado != EstadoVenta.CANCELADA) {
            throw new IllegalStateException("Can only cancel a completed venta");
        }
    }

    /**
     * Business logic: Cancel venta
     */
    public void cancelar() {
        this.estado = EstadoVenta.CANCELADA;
    }

    /**
     * Business logic: Confirm venta
     */
    public void confirmar() {
        if (this.estado != EstadoVenta.BORRADOR) {
            throw new IllegalStateException("Only BORRADOR ventas can be confirmed");
        }
        if (detalles.isEmpty()) {
            throw new IllegalStateException("Cannot confirm venta without detalles");
        }
        this.estado = EstadoVenta.CONFIRMADA;
    }

    /**
     * Soft delete implementation
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Check if venta is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if venta can be modified
     */
    public boolean isModifiable() {
        return estado == EstadoVenta.BORRADOR;
    }

    /**
     * Validate venta invariants
     */
    public void validate() {
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        if (descuento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Descuento cannot be negative");
        }
        if (impuestos.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Impuestos cannot be negative");
        }
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total cannot be negative");
        }
    }
}
