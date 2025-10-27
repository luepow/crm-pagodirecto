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
 * Entidad de dominio: Usuario
 *
 * Representa un usuario del sistema con soporte para autenticación multi-factor (MFA)
 * y control de intentos fallidos de login.
 *
 * Tabla: seguridad_usuarios
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "seguridad_usuarios", indexes = {
    @Index(name = "idx_seguridad_usuarios_username", columnList = "username"),
    @Index(name = "idx_seguridad_usuarios_email", columnList = "email"),
    @Index(name = "idx_seguridad_usuarios_unidad_negocio", columnList = "unidad_negocio_id"),
    @Index(name = "idx_seguridad_usuarios_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE seguridad_usuarios SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre_completo", length = 100)
    private String nombreCompleto;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 255)
    private String mfaSecret;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UsuarioStatus status = UsuarioStatus.ACTIVE;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

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
        name = "seguridad_usuarios_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    /**
     * Verifica si el usuario está activo
     *
     * @return true si el status es ACTIVE, false en caso contrario
     */
    public boolean isActive() {
        return UsuarioStatus.ACTIVE.equals(this.status);
    }

    /**
     * Verifica si el usuario está bloqueado
     *
     * @return true si está bloqueado y el tiempo de bloqueo no ha expirado
     */
    public boolean isLocked() {
        if (UsuarioStatus.LOCKED.equals(this.status)) {
            return true;
        }
        if (bloqueadoHasta != null && Instant.now().isBefore(bloqueadoHasta)) {
            return true;
        }
        return false;
    }

    /**
     * Incrementa el contador de intentos fallidos
     * Si alcanza el máximo (5), bloquea la cuenta por 30 minutos
     */
    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 5) {
            this.status = UsuarioStatus.LOCKED;
            this.bloqueadoHasta = Instant.now().plusSeconds(1800); // 30 minutos
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Resetea el contador de intentos fallidos tras un login exitoso
     */
    public void resetearIntentosFallidos() {
        this.intentosFallidos = 0;
        if (UsuarioStatus.LOCKED.equals(this.status) && bloqueadoHasta != null
            && Instant.now().isAfter(bloqueadoHasta)) {
            this.status = UsuarioStatus.ACTIVE;
            this.bloqueadoHasta = null;
        }
        this.ultimoAcceso = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Actualiza el timestamp de último acceso
     */
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Bloquea el usuario
     *
     * @param duracionSegundos duración del bloqueo en segundos (null = indefinido)
     */
    public void bloquear(Integer duracionSegundos) {
        this.status = UsuarioStatus.LOCKED;
        if (duracionSegundos != null) {
            this.bloqueadoHasta = Instant.now().plusSeconds(duracionSegundos);
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Desbloquea el usuario
     */
    public void desbloquear() {
        if (UsuarioStatus.LOCKED.equals(this.status)) {
            this.status = UsuarioStatus.ACTIVE;
            this.bloqueadoHasta = null;
            this.intentosFallidos = 0;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Asigna un rol al usuario
     *
     * @param rol el rol a asignar
     */
    public void asignarRol(Rol rol) {
        this.roles.add(rol);
        this.updatedAt = Instant.now();
    }

    /**
     * Remueve un rol del usuario
     *
     * @param rol el rol a remover
     */
    public void removerRol(Rol rol) {
        this.roles.remove(rol);
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
