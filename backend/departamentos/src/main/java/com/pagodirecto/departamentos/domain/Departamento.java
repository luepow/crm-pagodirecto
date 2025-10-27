package com.pagodirecto.departamentos.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Departamento
 *
 * Representa un departamento organizacional con jerarquía tipo árbol
 *
 * Tabla: departamentos
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "departamentos", indexes = {
    @Index(name = "idx_departamentos_unidad_negocio", columnList = "unidad_negocio_id"),
    @Index(name = "idx_departamentos_parent", columnList = "parent_id"),
    @Index(name = "idx_departamentos_jefe", columnList = "jefe_id"),
    @Index(name = "idx_departamentos_codigo", columnList = "codigo"),
    @Index(name = "idx_departamentos_activo", columnList = "activo")
})
@SQLDelete(sql = "UPDATE departamentos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

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

    @Column(name = "jefe_id")
    private UUID jefeId;

    @Column(name = "email_departamento", length = 255)
    private String emailDepartamento;

    @Column(name = "telefono_departamento", length = 50)
    private String telefonoDepartamento;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "presupuesto_anual", precision = 15, scale = 2)
    private BigDecimal presupuestoAnual;

    @Column(name = "numero_empleados")
    @Builder.Default
    private Integer numeroEmpleados = 0;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

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

    /**
     * Construye el path jerárquico del departamento
     */
    public void buildPath(String parentPath) {
        if (parentPath != null && !parentPath.isEmpty()) {
            this.path = parentPath + "/" + this.nombre;
        } else {
            this.path = "/" + this.nombre;
        }
    }
}
