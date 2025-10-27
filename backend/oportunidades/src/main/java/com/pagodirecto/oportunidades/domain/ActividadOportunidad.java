package com.pagodirecto.oportunidades.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Actividad de Oportunidad
 *
 * Representa actividades relacionadas con oportunidades (llamadas, reuniones, emails).
 *
 * Tabla: oportunidades_actividades
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "oportunidades_actividades", indexes = {
    @Index(name = "idx_oportunidades_actividades_oportunidad", columnList = "oportunidad_id"),
    @Index(name = "idx_oportunidades_actividades_tipo", columnList = "tipo"),
    @Index(name = "idx_oportunidades_actividades_completada", columnList = "completada")
})
@SQLDelete(sql = "UPDATE oportunidades_actividades SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadOportunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oportunidad_id", nullable = false)
    private Oportunidad oportunidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoActividad tipo;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_actividad", nullable = false)
    private Instant fechaActividad;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "completada", nullable = false)
    @Builder.Default
    private Boolean completada = false;

    @Column(name = "resultado", columnDefinition = "TEXT")
    private String resultado;

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

    /**
     * Marca la actividad como completada
     *
     * @param resultado resultado de la actividad
     */
    public void completar(String resultado) {
        this.completada = true;
        this.resultado = resultado;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
