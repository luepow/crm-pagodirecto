package com.pagodirecto.ventas.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Item de Cotización
 *
 * Representa una línea de detalle en una cotización.
 *
 * Tabla: ventas_items_cotizacion
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "ventas_items_cotizacion", indexes = {
    @Index(name = "idx_ventas_items_cotizacion_cotizacion", columnList = "cotizacion_id"),
    @Index(name = "idx_ventas_items_cotizacion_producto", columnList = "producto_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "descuento_porcentaje", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;

    @Column(name = "descuento_monto", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal descuentoMonto = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "impuesto_porcentaje", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal impuestoPorcentaje = BigDecimal.ZERO;

    @Column(name = "impuesto_monto", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal impuestoMonto = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "orden")
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by")
    private UUID updatedBy;

    public void calcularMontos() {
        // Calcular subtotal: cantidad * precio_unitario
        this.subtotal = cantidad.multiply(precioUnitario);

        // Aplicar descuento
        if (descuentoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
            this.descuentoMonto = subtotal.multiply(descuentoPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal subtotalConDescuento = subtotal.subtract(descuentoMonto);

        // Calcular impuesto
        if (impuestoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
            this.impuestoMonto = subtotalConDescuento.multiply(impuestoPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }

        // Calcular total
        this.total = subtotalConDescuento.add(impuestoMonto);
        this.updatedAt = Instant.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calcularMontos();
        this.updatedAt = Instant.now();
    }
}
