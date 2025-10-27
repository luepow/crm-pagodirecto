package com.pagodirecto.tareas.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Tarea
 *
 * Representa una tarea asignable con relaciones polimórficas a cualquier entidad.
 *
 * Tabla: tareas_tareas
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "tareas_tareas", indexes = {
    @Index(name = "idx_tareas_tareas_asignado", columnList = "asignado_a"),
    @Index(name = "idx_tareas_tareas_status", columnList = "status"),
    @Index(name = "idx_tareas_tareas_prioridad", columnList = "prioridad"),
    @Index(name = "idx_tareas_tareas_relacionado", columnList = "relacionado_tipo, relacionado_id")
})
@SQLDelete(sql = "UPDATE tareas_tareas SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoTarea tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    @Builder.Default
    private PrioridadTarea prioridad = PrioridadTarea.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusTarea status = StatusTarea.PENDIENTE;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_completada")
    private Instant fechaCompletada;

    @Column(name = "asignado_a", nullable = false)
    private UUID asignadoA;

    @Column(name = "relacionado_tipo", length = 50)
    private String relacionadoTipo;

    @Column(name = "relacionado_id")
    private UUID relacionadoId;

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

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ComentarioTarea> comentarios = new HashSet<>();

    public boolean isVencida() {
        return fechaVencimiento != null && LocalDate.now().isAfter(fechaVencimiento) &&
               !StatusTarea.COMPLETADA.equals(status);
    }

    public void completar() {
        this.status = StatusTarea.COMPLETADA;
        this.fechaCompletada = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancelar() {
        this.status = StatusTarea.CANCELADA;
        this.updatedAt = Instant.now();
    }

    public void asignar(UUID usuarioId) {
        this.asignadoA = usuarioId;
        this.updatedAt = Instant.now();
    }

    public void agregarComentario(ComentarioTarea comentario) {
        comentario.setTarea(this);
        this.comentarios.add(comentario);
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
