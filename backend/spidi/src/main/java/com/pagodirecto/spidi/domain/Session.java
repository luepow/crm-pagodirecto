package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Sesión de Conexión
 *
 * Representa una sesión de usuario conectado a una sala Spidi.
 * Incluye tracking de heartbeat, latencia y metadatos del cliente.
 * Tabla: dat_spd_session
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "dat_spd_session", indexes = {
    @Index(name = "idx_dat_spd_session_room", columnList = "room_id"),
    @Index(name = "idx_dat_spd_session_user", columnList = "user_id"),
    @Index(name = "idx_dat_spd_session_client", columnList = "client_id"),
    @Index(name = "idx_dat_spd_session_started", columnList = "started_at"),
    @Index(name = "idx_dat_spd_session_last_heartbeat", columnList = "last_heartbeat_at"),
    @Index(name = "idx_dat_spd_session_status", columnList = "status"),
    @Index(name = "idx_dat_spd_session_active_room", columnList = "room_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "client_id", nullable = false, length = 255)
    private String clientId;

    @Column(name = "device", length = 100)
    private String device;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "ip_address", nullable = false, columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "started_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "last_heartbeat_at", nullable = false)
    @Builder.Default
    private Instant lastHeartbeatAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "avg_latency_ms")
    private Integer avgLatencyMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "disconnect_reason", length = 100)
    private String disconnectReason;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // Transient field for latency calculation
    @Transient
    private static final double LATENCY_ALPHA = 0.3; // Smoothing factor for rolling average

    /**
     * Registra un heartbeat y actualiza la latencia promedio
     *
     * @param latencyMs latencia reportada en milisegundos
     */
    public void recordHeartbeat(Integer latencyMs) {
        this.lastHeartbeatAt = Instant.now();
        this.updatedAt = Instant.now();

        if (latencyMs != null && latencyMs > 0) {
            // Calculate rolling average using exponential smoothing
            if (this.avgLatencyMs == null) {
                this.avgLatencyMs = latencyMs;
            } else {
                this.avgLatencyMs = (int) (LATENCY_ALPHA * latencyMs + (1 - LATENCY_ALPHA) * this.avgLatencyMs);
            }
        }
    }

    /**
     * Verifica si la sesión ha expirado por timeout de heartbeat
     *
     * @param timeoutSeconds segundos sin heartbeat para considerar expirada
     * @return true si la sesión está expirada
     */
    public boolean isExpired(int timeoutSeconds) {
        if (this.status != SessionStatus.ACTIVE) {
            return false; // Solo las sesiones activas pueden expirar
        }

        Duration timeSinceLastHeartbeat = Duration.between(lastHeartbeatAt, Instant.now());
        return timeSinceLastHeartbeat.getSeconds() > timeoutSeconds;
    }

    /**
     * Verifica si la sesión está activa
     *
     * @return true si el status es ACTIVE
     */
    public boolean isActive() {
        return SessionStatus.ACTIVE.equals(this.status);
    }

    /**
     * Marca la sesión como expirada
     *
     * @param reason razón de expiración
     */
    public void expire(String reason) {
        if (this.status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.EXPIRED;
            this.endedAt = Instant.now();
            this.disconnectReason = reason != null ? reason : "TIMEOUT";
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Desconecta la sesión por acción del usuario
     */
    public void disconnect() {
        if (this.status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.DISCONNECTED;
            this.endedAt = Instant.now();
            this.disconnectReason = "USER_LEAVE";
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Termina la sesión forzadamente (por admin o servidor)
     *
     * @param reason razón de terminación
     */
    public void terminate(String reason) {
        if (this.status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.TERMINATED;
            this.endedAt = Instant.now();
            this.disconnectReason = reason != null ? reason : "SERVER_SHUTDOWN";
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Calcula la duración de la sesión
     *
     * @return duración en segundos
     */
    public long getDurationSeconds() {
        Instant end = endedAt != null ? endedAt : Instant.now();
        return Duration.between(startedAt, end).getSeconds();
    }

    /**
     * Calcula el tiempo desde el último heartbeat
     *
     * @return segundos desde el último heartbeat
     */
    public long getSecondsSinceLastHeartbeat() {
        return Duration.between(lastHeartbeatAt, Instant.now()).getSeconds();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
