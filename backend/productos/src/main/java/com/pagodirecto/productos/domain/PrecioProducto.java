package com.pagodirecto.productos.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad de dominio: Precio de Producto
 *
 * Representa reglas de precios diferenciados (mayoreo, promoción, etc.).
 *
 * Tabla: productos_precios
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "productos_precios", indexes = {
    @Index(name = "idx_productos_precios_producto", columnList = "producto_id"),
    @Index(name = "idx_productos_precios_tipo", columnList = "tipo_precio"),
    @Index(name = "idx_productos_precios_vigencia", columnList = "fecha_inicio, fecha_fin")
})
@SQLDelete(sql = "UPDATE productos_precios SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "tipo_precio", nullable = false, length = 50)
    private String tipoPrecio;

    @Column(name = "precio", nullable = false, precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "MXN";

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "segmento_cliente", length = 50)
    private String segmentoCliente;

    @Column(name = "cantidad_minima")
    @Builder.Default
    private Integer cantidadMinima = 1;

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

    public boolean esVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && (fechaFin == null || !hoy.isAfter(fechaFin));
    }

    public boolean aplicaParaCantidad(Integer cantidad) {
        return cantidad >= cantidadMinima;
    }

    public boolean aplicaParaSegmento(String segmento) {
        return segmentoCliente == null || segmentoCliente.equals(segmento);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
