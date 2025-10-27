package com.pagodirecto.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad de dominio: AuditLog
 *
 * Registro inmutable de auditoría (append-only).
 * Retención: 7 años para cumplimiento financiero.
 *
 * Tabla: seguridad_audit_log
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "seguridad_audit_log", indexes = {
    @Index(name = "idx_seguridad_audit_log_usuario", columnList = "usuario_id"),
    @Index(name = "idx_seguridad_audit_log_accion", columnList = "accion"),
    @Index(name = "idx_seguridad_audit_log_recurso", columnList = "recurso"),
    @Index(name = "idx_seguridad_audit_log_created", columnList = "created_at"),
    @Index(name = "idx_seguridad_audit_log_resultado", columnList = "resultado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @Column(name = "recurso", nullable = false, length = 100)
    private String recurso;

    @Column(name = "recurso_id")
    private UUID recursoId;

    @Column(name = "ip_address", nullable = false, columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private AuditResultado resultado;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Verifica si la auditoría fue exitosa
     *
     * @return true si el resultado es SUCCESS, false en caso contrario
     */
    public boolean isSuccess() {
        return AuditResultado.SUCCESS.equals(this.resultado);
    }

    /**
     * Verifica si la auditoría falló
     *
     * @return true si el resultado es FAILURE, false en caso contrario
     */
    public boolean isFailure() {
        return AuditResultado.FAILURE.equals(this.resultado);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog)) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && id.equals(auditLog.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
