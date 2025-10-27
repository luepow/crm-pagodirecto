package com.pagodirecto.reportes.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Dashboard
 *
 * Representa un dashboard personalizable con configuración JSONB.
 *
 * Tabla: reportes_dashboards
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "reportes_dashboards", indexes = {
    @Index(name = "idx_reportes_dashboards_propietario", columnList = "propietario_id"),
    @Index(name = "idx_reportes_dashboards_publico", columnList = "es_publico")
})
@SQLDelete(sql = "UPDATE reportes_dashboards SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuracion", columnDefinition = "jsonb")
    private Map<String, Object> configuracion;

    @Column(name = "propietario_id", nullable = false)
    private UUID propietarioId;

    @Column(name = "es_publico", nullable = false)
    @Builder.Default
    private Boolean esPublico = false;

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

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<WidgetDashboard> widgets = new HashSet<>();

    public void hacerPublico() {
        this.esPublico = true;
        this.updatedAt = Instant.now();
    }

    public void hacerPrivado() {
        this.esPublico = false;
        this.updatedAt = Instant.now();
    }

    public void agregarWidget(WidgetDashboard widget) {
        widget.setDashboard(this);
        this.widgets.add(widget);
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
