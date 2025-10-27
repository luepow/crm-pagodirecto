package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Estadísticas de Sala
 *
 * Estadísticas agregadas por intervalos de tiempo (time-series).
 * Permite análisis histórico de concurrencia, latencia y actividad.
 * Tabla: dat_spd_room_stats
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "dat_spd_room_stats", indexes = {
    @Index(name = "idx_dat_spd_room_stats_room", columnList = "room_id"),
    @Index(name = "idx_dat_spd_room_stats_ts_bucket", columnList = "ts_bucket"),
    @Index(name = "idx_dat_spd_room_stats_room_ts", columnList = "room_id, ts_bucket")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_dat_spd_room_stats_bucket", columnNames = {"room_id", "ts_bucket", "bucket_interval"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "ts_bucket", nullable = false)
    private Instant tsBucket;

    @Enumerated(EnumType.STRING)
    @Column(name = "bucket_interval", nullable = false, length = 20)
    @Builder.Default
    private BucketInterval bucketInterval = BucketInterval.HOUR;

    @Column(name = "count_online", nullable = false)
    @Builder.Default
    private Integer countOnline = 0;

    @Column(name = "peak_online", nullable = false)
    @Builder.Default
    private Integer peakOnline = 0;

    @Column(name = "avg_latency_ms")
    private Integer avgLatencyMs;

    @Column(name = "total_sessions", nullable = false)
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "total_connects", nullable = false)
    @Builder.Default
    private Integer totalConnects = 0;

    @Column(name = "total_disconnects", nullable = false)
    @Builder.Default
    private Integer totalDisconnects = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    /**
     * Actualiza el conteo de usuarios online
     *
     * @param onlineCount conteo actual
     */
    public void updateOnlineCount(int onlineCount) {
        this.countOnline = onlineCount;
        if (onlineCount > this.peakOnline) {
            this.peakOnline = onlineCount;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Incrementa el contador de conexiones
     */
    public void incrementConnects() {
        this.totalConnects++;
        this.updatedAt = Instant.now();
    }

    /**
     * Incrementa el contador de desconexiones
     */
    public void incrementDisconnects() {
        this.totalDisconnects++;
        this.updatedAt = Instant.now();
    }

    /**
     * Actualiza la latencia promedio
     *
     * @param latencyMs nueva latencia en milisegundos
     */
    public void updateAverageLatency(int latencyMs) {
        if (this.avgLatencyMs == null) {
            this.avgLatencyMs = latencyMs;
        } else {
            // Simple moving average
            this.avgLatencyMs = (this.avgLatencyMs + latencyMs) / 2;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Calcula el porcentaje de uptime (sesiones activas vs desconexiones)
     *
     * @return porcentaje de uptime (0.0 - 100.0)
     */
    public double getUptimePercentage() {
        int total = totalConnects + totalDisconnects;
        if (total == 0) {
            return 100.0;
        }
        return (totalConnects * 100.0) / total;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
