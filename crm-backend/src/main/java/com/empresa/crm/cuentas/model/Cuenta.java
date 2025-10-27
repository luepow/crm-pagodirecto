package com.empresa.crm.cuentas.model;

import com.empresa.crm.clientes.model.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity for accounts payable/receivable.
 * Bounded context: Cuentas
 * Aligned with V6 migration schema
 */
@Entity
@Table(name = "cuentas", indexes = {
    @Index(name = "idx_cuenta_folio", columnList = "folio", unique = true),
    @Index(name = "idx_cuenta_tipo", columnList = "tipo"),
    @Index(name = "idx_cuenta_estado", columnList = "estado"),
    @Index(name = "idx_cuenta_cliente", columnList = "cliente_id"),
    @Index(name = "idx_cuenta_vencimiento", columnList = "fecha_vencimiento"),
    @Index(name = "idx_cuenta_referencia", columnList = "referencia_tipo,referencia_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String folio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;

    @Column(name = "referencia_tipo", nullable = false, length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE;

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
        if (saldo == null || saldo.compareTo(BigDecimal.ZERO) == 0) {
            saldo = monto;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        checkVencimiento();
    }

    /**
     * Business logic: Marcar como pagada
     */
    public void marcarPagada() {
        if (this.estado == EstadoCuenta.CANCELADO) {
            throw new IllegalStateException("Cannot mark CANCELADO cuenta as PAGADO");
        }
        this.estado = EstadoCuenta.PAGADO;
        this.saldo = BigDecimal.ZERO;
    }

    /**
     * Business logic: Aplicar pago parcial
     */
    public void aplicarPago(BigDecimal montoPago) {
        if (this.estado == EstadoCuenta.CANCELADO) {
            throw new IllegalStateException("Cannot apply payment to CANCELADO cuenta");
        }
        if (this.estado == EstadoCuenta.PAGADO) {
            throw new IllegalStateException("Cuenta already PAGADO");
        }
        if (montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto pago must be positive");
        }
        if (montoPago.compareTo(this.saldo) > 0) {
            throw new IllegalArgumentException("Monto pago cannot exceed saldo");
        }

        this.saldo = this.saldo.subtract(montoPago);

        // If fully paid, mark as PAGADO
        if (this.saldo.compareTo(BigDecimal.ZERO) == 0) {
            this.estado = EstadoCuenta.PAGADO;
        }
    }

    /**
     * Business logic: Cancelar cuenta
     */
    public void cancelar() {
        this.estado = EstadoCuenta.CANCELADO;
    }

    /**
     * Business logic: Check if cuenta is vencida
     */
    public boolean isVencida() {
        LocalDate today = LocalDate.now();
        return this.estado == EstadoCuenta.PENDIENTE &&
               this.fechaVencimiento.isBefore(today);
    }

    /**
     * Business logic: Auto-update estado if vencida
     */
    public void checkVencimiento() {
        if (isVencida() && this.estado == EstadoCuenta.PENDIENTE) {
            this.estado = EstadoCuenta.VENCIDO;
        }
    }

    /**
     * Business logic: Calculate dias vencidos
     */
    public int getDiasVencidos() {
        if (!isVencida() && this.estado != EstadoCuenta.VENCIDO) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(this.fechaVencimiento, today);
    }

    /**
     * Soft delete implementation
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Check if cuenta is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Validate cuenta invariants
     */
    public void validate() {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto must be positive");
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo cannot be negative");
        }
        if (saldo.compareTo(monto) > 0) {
            throw new IllegalArgumentException("Saldo cannot exceed monto");
        }
        if (fechaVencimiento.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("Fecha vencimiento cannot be before fecha emision");
        }
    }

    /**
     * Check if cuenta can be modified
     */
    public boolean isModifiable() {
        return estado == EstadoCuenta.PENDIENTE || estado == EstadoCuenta.VENCIDO;
    }
}
