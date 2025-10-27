package com.pagodirecto.productos.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Producto
 *
 * Representa un producto o servicio en el catálogo.
 *
 * Tabla: productos_productos
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "productos_productos", indexes = {
    @Index(name = "idx_productos_productos_codigo", columnList = "codigo"),
    @Index(name = "idx_productos_productos_sku", columnList = "sku"),
    @Index(name = "idx_productos_productos_categoria", columnList = "categoria_id"),
    @Index(name = "idx_productos_productos_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE productos_productos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "codigo", nullable = false, length = 100, unique = true)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaProducto categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @Builder.Default
    private ProductoTipo tipo = ProductoTipo.PRODUCTO;

    @Column(name = "precio_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "MXN";

    @Column(name = "costo_unitario", precision = 15, scale = 2)
    private BigDecimal costoUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductoStatus status = ProductoStatus.ACTIVE;

    @Column(name = "stock_actual")
    @Builder.Default
    private Integer stockActual = 0;

    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 0;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @Column(name = "peso_kg", precision = 10, scale = 3)
    private BigDecimal pesoKg;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

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

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PrecioProducto> precios = new HashSet<>();

    public boolean isActive() {
        return ProductoStatus.ACTIVE.equals(this.status);
    }

    public boolean requiereReabastecimiento() {
        return stockActual <= stockMinimo;
    }

    public BigDecimal calcularMargenBruto() {
        if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precioBase.subtract(costoUnitario).divide(precioBase, 4, BigDecimal.ROUND_HALF_UP)
               .multiply(BigDecimal.valueOf(100));
    }

    public void actualizarStock(Integer cantidad) {
        this.stockActual += cantidad;
        this.updatedAt = Instant.now();
    }

    public void activar() {
        this.status = ProductoStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void desactivar() {
        this.status = ProductoStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void descontinuar() {
        this.status = ProductoStatus.DISCONTINUED;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
