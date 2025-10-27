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
 * Entidad de dominio: Cotización
 *
 * Representa una cotización enviada a un cliente.
 *
 * Tabla: ventas_cotizaciones
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "ventas_cotizaciones", indexes = {
    @Index(name = "idx_ventas_cotizaciones_cliente", columnList = "cliente_id"),
    @Index(name = "idx_ventas_cotizaciones_numero", columnList = "numero"),
    @Index(name = "idx_ventas_cotizaciones_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE ventas_cotizaciones SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "oportunidad_id")
    private UUID oportunidadId;

    @Column(name = "numero", nullable = false, length = 50, unique = true)
    private String numero;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private LocalDate fecha = LocalDate.now();

    @Column(name = "fecha_validez", nullable = false)
    private LocalDate fechaValidez;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CotizacionStatus status = CotizacionStatus.BORRADOR;

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

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "terminos_condiciones", columnDefinition = "TEXT")
    private String terminosCondiciones;

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

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ItemCotizacion> items = new HashSet<>();

    public void calcularTotales() {
        this.subtotal = items.stream()
            .map(ItemCotizacion::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotalConDescuento = subtotal.subtract(descuentoGlobal);

        this.impuestos = items.stream()
            .map(ItemCotizacion::getImpuestoMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = subtotalConDescuento.add(impuestos);
        this.updatedAt = Instant.now();
    }

    public void enviar() {
        if (CotizacionStatus.BORRADOR.equals(this.status)) {
            this.status = CotizacionStatus.ENVIADA;
            this.updatedAt = Instant.now();
        }
    }

    public void aceptar() {
        if (CotizacionStatus.ENVIADA.equals(this.status)) {
            this.status = CotizacionStatus.ACEPTADA;
            this.updatedAt = Instant.now();
        }
    }

    public void rechazar() {
        if (CotizacionStatus.ENVIADA.equals(this.status)) {
            this.status = CotizacionStatus.RECHAZADA;
            this.updatedAt = Instant.now();
        }
    }

    public boolean isExpirada() {
        return LocalDate.now().isAfter(fechaValidez);
    }

    public void agregarItem(ItemCotizacion item) {
        item.setCotizacion(this);
        this.items.add(item);
        calcularTotales();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
