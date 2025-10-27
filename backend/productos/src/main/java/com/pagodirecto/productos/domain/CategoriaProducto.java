package com.pagodirecto.productos.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Categoría de Producto
 *
 * Representa una categoría jerárquica para organizar productos (hasta 5 niveles).
 *
 * Tabla: productos_categorias
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "productos_categorias", indexes = {
    @Index(name = "idx_productos_categorias_parent", columnList = "parent_id"),
    @Index(name = "idx_productos_categorias_nivel", columnList = "nivel")
})
@SQLDelete(sql = "UPDATE productos_categorias SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "nivel", nullable = false)
    @Builder.Default
    private Integer nivel = 0;

    @Column(name = "path", length = 500)
    private String path;

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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Producto> productos = new HashSet<>();

    public boolean isRaiz() {
        return parentId == null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
