package com.empresa.crm.pagos.model;

import com.empresa.crm.ventas.model.Venta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity for payments.
 * Bounded context: Pagos
 * Aligned with V5 migration schema
 */
@Entity
@Table(name = "pagos", indexes = {
    @Index(name = "idx_pago_folio", columnList = "folio", unique = true),
    @Index(name = "idx_pago_venta", columnList = "venta_id"),
    @Index(name = "idx_pago_fecha", columnList = "fecha_pago"),
    @Index(name = "idx_pago_metodo", columnList = "metodo_pago"),
    @Index(name = "idx_pago_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(length = 100)
    private String referencia;

    @Column(columnDefinition = "TEXT")
    private String notas;

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
     * Business logic: Completar pago
     */
    public void completar() {
        if (this.estado != EstadoPago.PENDIENTE) {
            throw new IllegalStateException("Only PENDIENTE pagos can be completed");
        }
        this.estado = EstadoPago.COMPLETADO;
    }

    /**
     * Business logic: Marcar como fallido
     */
    public void marcarFallido() {
        if (this.estado == EstadoPago.COMPLETADO) {
            throw new IllegalStateException("Cannot mark COMPLETADO pago as FALLIDO");
        }
        this.estado = EstadoPago.FALLIDO;
    }

    /**
     * Business logic: Reembolsar pago
     */
    public void reembolsar() {
        if (this.estado != EstadoPago.COMPLETADO) {
            throw new IllegalStateException("Only COMPLETADO pagos can be refunded");
        }
        this.estado = EstadoPago.REEMBOLSADO;
    }

    /**
     * Business logic: Change estado
     */
    public void cambiarEstado(EstadoPago nuevoEstado) {
        validateEstadoTransition(nuevoEstado);
        this.estado = nuevoEstado;
    }

    /**
     * Validate estado transition rules
     */
    private void validateEstadoTransition(EstadoPago nuevoEstado) {
        if (this.estado == EstadoPago.REEMBOLSADO) {
            throw new IllegalStateException("Cannot change estado from REEMBOLSADO");
        }

        // PENDIENTE can go to COMPLETADO or FALLIDO
        if (this.estado == EstadoPago.PENDIENTE) {
            if (nuevoEstado != EstadoPago.COMPLETADO && nuevoEstado != EstadoPago.FALLIDO) {
                throw new IllegalStateException(
                    "PENDIENTE can only transition to COMPLETADO or FALLIDO");
            }
        }

        // COMPLETADO can only go to REEMBOLSADO
        if (this.estado == EstadoPago.COMPLETADO) {
            if (nuevoEstado != EstadoPago.REEMBOLSADO) {
                throw new IllegalStateException(
                    "COMPLETADO can only transition to REEMBOLSADO");
            }
        }

        // FALLIDO cannot transition to anything
        if (this.estado == EstadoPago.FALLIDO) {
            throw new IllegalStateException("Cannot change estado from FALLIDO");
        }
    }

    /**
     * Soft delete implementation
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Check if pago is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Validate pago invariants
     */
    public void validate() {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto must be positive");
        }
    }

    /**
     * Check if pago can be modified
     */
    public boolean isModifiable() {
        return estado == EstadoPago.PENDIENTE;
    }
}
