package com.pagodirecto.oportunidades.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Etapa del Pipeline
 *
 * Representa una etapa configurable del pipeline de ventas.
 *
 * Tabla: oportunidades_etapas_pipeline
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "oportunidades_etapas_pipeline", indexes = {
    @Index(name = "idx_oportunidades_etapas_unidad_negocio", columnList = "unidad_negocio_id"),
    @Index(name = "idx_oportunidades_etapas_tipo", columnList = "tipo"),
    @Index(name = "idx_oportunidades_etapas_orden", columnList = "orden")
})
@SQLDelete(sql = "UPDATE oportunidades_etapas_pipeline SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaPipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoEtapa tipo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "probabilidad_default", precision = 5, scale = 2)
    private BigDecimal probabilidadDefault;

    @Column(name = "color", length = 7)
    private String color;

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
