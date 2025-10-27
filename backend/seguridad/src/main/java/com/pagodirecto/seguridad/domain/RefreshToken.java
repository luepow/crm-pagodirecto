package com.pagodirecto.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: RefreshToken
 *
 * Representa un token de refresco JWT para autenticación.
 * TTL típico: 30 días. Se almacena el hash SHA-256 del token.
 *
 * Tabla: seguridad_refresh_tokens
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "seguridad_refresh_tokens", indexes = {
    @Index(name = "idx_seguridad_refresh_tokens_usuario", columnList = "usuario_id"),
    @Index(name = "idx_seguridad_refresh_tokens_token", columnList = "token_hash"),
    @Index(name = "idx_seguridad_refresh_tokens_expires", columnList = "expires_at"),
    @Index(name = "idx_seguridad_refresh_tokens_revocado", columnList = "revocado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revocado", nullable = false)
    @Builder.Default
    private Boolean revocado = false;

    @Column(name = "revocado_at")
    private Instant revocadoAt;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Verifica si el token ha expirado
     *
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Verifica si el token es válido (no revocado y no expirado)
     *
     * @return true si es válido, false en caso contrario
     */
    public boolean isValid() {
        return !revocado && !isExpired();
    }

    /**
     * Revoca el token
     */
    public void revocar() {
        this.revocado = true;
        this.revocadoAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken)) return false;
        RefreshToken that = (RefreshToken) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
