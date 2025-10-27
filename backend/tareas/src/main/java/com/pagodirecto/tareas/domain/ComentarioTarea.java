package com.pagodirecto.tareas.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Comentario de Tarea
 *
 * Representa un comentario o actualización en una tarea.
 *
 * Tabla: tareas_comentarios
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "tareas_comentarios", indexes = {
    @Index(name = "idx_tareas_comentarios_tarea", columnList = "tarea_id"),
    @Index(name = "idx_tareas_comentarios_usuario", columnList = "usuario_id")
})
@SQLDelete(sql = "UPDATE tareas_comentarios SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "comentario", nullable = false, columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
