package com.pagodirecto.ventas.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Item de Pedido
 *
 * Representa una línea de detalle en un pedido con seguimiento de entregas.
 *
 * Tabla: ventas_items_pedido
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "ventas_items_pedido", indexes = {
    @Index(name = "idx_ventas_items_pedido_pedido", columnList = "pedido_id"),
    @Index(name = "idx_ventas_items_pedido_producto", columnList = "producto_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "cantidad_entregada", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal cantidadEntregada = BigDecimal.ZERO;

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
        this.subtotal = cantidad.multiply(precioUnitario);

        if (descuentoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
            this.descuentoMonto = subtotal.multiply(descuentoPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal subtotalConDescuento = subtotal.subtract(descuentoMonto);

        if (impuestoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
            this.impuestoMonto = subtotalConDescuento.multiply(impuestoPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }

        this.total = subtotalConDescuento.add(impuestoMonto);
        this.updatedAt = Instant.now();
    }

    public void registrarEntrega(BigDecimal cantidadAEntregar) {
        BigDecimal nuevaCantidadEntregada = this.cantidadEntregada.add(cantidadAEntregar);
        if (nuevaCantidadEntregada.compareTo(cantidad) > 0) {
            throw new IllegalArgumentException("No se puede entregar más de lo ordenado");
        }
        this.cantidadEntregada = nuevaCantidadEntregada;
        this.updatedAt = Instant.now();
    }

    public boolean isEntregaCompleta() {
        return cantidadEntregada.compareTo(cantidad) >= 0;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calcularMontos();
        this.updatedAt = Instant.now();
    }
}
