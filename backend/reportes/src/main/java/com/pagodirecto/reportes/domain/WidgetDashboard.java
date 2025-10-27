package com.pagodirecto.reportes.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad de dominio: Widget de Dashboard
 *
 * Representa un widget individual dentro de un dashboard.
 *
 * Tabla: reportes_widgets
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "reportes_widgets", indexes = {
    @Index(name = "idx_reportes_widgets_dashboard", columnList = "dashboard_id"),
    @Index(name = "idx_reportes_widgets_tipo", columnList = "tipo")
})
@SQLDelete(sql = "UPDATE reportes_widgets SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private WidgetTipo tipo;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuracion", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> configuracion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "posicion", columnDefinition = "jsonb")
    private Map<String, Object> posicion;

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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
