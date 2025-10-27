package com.pagodirecto.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Permiso
 *
 * Representa un permiso granular CRUD por recurso
 *
 * Tabla: seguridad_permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "seguridad_permisos", indexes = {
    @Index(name = "idx_seguridad_permisos_recurso", columnList = "recurso"),
    @Index(name = "idx_seguridad_permisos_accion", columnList = "accion")
})
@SQLDelete(sql = "UPDATE seguridad_permisos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "recurso", nullable = false, length = 100)
    private String recurso;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "scope", length = 100)
    private String scope;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

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
