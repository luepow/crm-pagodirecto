package com.pagodirecto.ventas.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Pedido (Orden de Venta)
 *
 * Representa un pedido confirmado de un cliente.
 *
 * Tabla: ventas_pedidos
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "ventas_pedidos", indexes = {
    @Index(name = "idx_ventas_pedidos_cliente", columnList = "cliente_id"),
    @Index(name = "idx_ventas_pedidos_numero", columnList = "numero"),
    @Index(name = "idx_ventas_pedidos_status", columnList = "status"),
    @Index(name = "idx_ventas_pedidos_cotizacion", columnList = "cotizacion_id")
})
@SQLDelete(sql = "UPDATE ventas_pedidos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "cotizacion_id")
    private UUID cotizacionId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "numero", nullable = false, length = 50, unique = true)
    private String numero;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDate fecha = LocalDate.now();

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    @Column(name = "fecha_entrega_real")
    private LocalDate fechaEntregaReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PedidoStatus status = PedidoStatus.PENDIENTE;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_global", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal descuentoGlobal = BigDecimal.ZERO;

    @Column(name = "impuestos", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal impuestos = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "MXN";

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "terminos_pago", length = 100)
    private String terminosPago;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "propietario_id", nullable = false)
    private UUID propietarioId;

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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ItemPedido> items = new HashSet<>();

    public void calcularTotales() {
        this.subtotal = items.stream()
            .map(ItemPedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotalConDescuento = subtotal.subtract(descuentoGlobal);

        this.impuestos = items.stream()
            .map(ItemPedido::getImpuestoMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = subtotalConDescuento.add(impuestos);
        this.updatedAt = Instant.now();
    }

    public void confirmar() {
        if (PedidoStatus.PENDIENTE.equals(this.status)) {
            this.status = PedidoStatus.CONFIRMADO;
            this.updatedAt = Instant.now();
        }
    }

    public void marcarEnProceso() {
        if (PedidoStatus.CONFIRMADO.equals(this.status)) {
            this.status = PedidoStatus.EN_PROCESO;
            this.updatedAt = Instant.now();
        }
    }

    public void marcarEnviado() {
        if (PedidoStatus.EN_PROCESO.equals(this.status)) {
            this.status = PedidoStatus.ENVIADO;
            this.updatedAt = Instant.now();
        }
    }

    public void marcarEntregado(LocalDate fechaEntrega) {
        if (PedidoStatus.ENVIADO.equals(this.status)) {
            this.status = PedidoStatus.ENTREGADO;
            this.fechaEntregaReal = fechaEntrega;
            this.updatedAt = Instant.now();
        }
    }

    public void cancelar() {
        this.status = PedidoStatus.CANCELADO;
        this.updatedAt = Instant.now();
    }

    public void agregarItem(ItemPedido item) {
        item.setPedido(this);
        this.items.add(item);
        calcularTotales();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
