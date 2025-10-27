package com.pagodirecto.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Rol
 *
 * Representa un rol del sistema con jerarquía departamental y permisos asociados
 *
 * Tabla: seguridad_roles
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "seguridad_roles", indexes = {
    @Index(name = "idx_seguridad_roles_unidad_negocio", columnList = "unidad_negocio_id"),
    @Index(name = "idx_seguridad_roles_departamento", columnList = "departamento")
})
@SQLDelete(sql = "UPDATE seguridad_roles SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "nivel_jerarquico", nullable = false)
    @Builder.Default
    private Integer nivelJerarquico = 0;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "seguridad_roles_permisos",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
