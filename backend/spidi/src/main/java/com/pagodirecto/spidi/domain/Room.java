package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Sala Spidi
 *
 * Representa una sala de comunicación (nodo Spidi) donde los usuarios se conectan.
 * Incluye configuración de capacidad, TTL, estado y metadatos personalizados.
 * Tabla: dat_spd_room
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "dat_spd_room", indexes = {
    @Index(name = "idx_dat_spd_room_unidad_negocio", columnList = "unidad_negocio_id"),
    @Index(name = "idx_dat_spd_room_code", columnList = "code"),
    @Index(name = "idx_dat_spd_room_status", columnList = "status"),
    @Index(name = "idx_dat_spd_room_type", columnList = "room_type_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_dat_spd_room_code", columnNames = {"unidad_negocio_id", "code"})
})
@SQLDelete(sql = "UPDATE dat_spd_room SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 100;

    @Column(name = "ttl_seconds", nullable = false)
    @Builder.Default
    private Integer ttlSeconds = 3600;

    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // Audit fields
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

    /**
     * Verifica si la sala está activa y operativa
     *
     * @return true si el status es ACTIVE
     */
    public boolean isActive() {
        return RoomStatus.ACTIVE.equals(this.status);
    }

    /**
     * Verifica si la sala acepta nuevas conexiones
     *
     * @return true si está activa y no está en mantenimiento
     */
    public boolean acceptsConnections() {
        return RoomStatus.ACTIVE.equals(this.status);
    }

    /**
     * Activa la sala
     */
    public void activate() {
        this.status = RoomStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Pone la sala en mantenimiento
     */
    public void putInMaintenance() {
        this.status = RoomStatus.MAINTENANCE;
        this.updatedAt = Instant.now();
    }

    /**
     * Deshabilita la sala
     */
    public void disable() {
        this.status = RoomStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Archiva la sala
     */
    public void archive() {
        this.status = RoomStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    /**
     * Calcula el porcentaje de capacidad utilizada
     *
     * @param currentOnline usuarios actualmente conectados
     * @return porcentaje de capacidad (0.0 - 100.0)
     */
    public double getCapacityPercentage(int currentOnline) {
        if (capacity <= 0) {
            return 0.0;
        }
        return Math.min((currentOnline * 100.0) / capacity, 100.0);
    }

    /**
     * Verifica si la sala está cerca de su capacidad máxima
     *
     * @param currentOnline usuarios actualmente conectados
     * @param threshold porcentaje de umbral (ej: 80.0 para 80%)
     * @return true si la capacidad actual >= threshold
     */
    public boolean isNearCapacity(int currentOnline, double threshold) {
        return getCapacityPercentage(currentOnline) >= threshold;
    }

    /**
     * Verifica si la sala está llena
     *
     * @param currentOnline usuarios actualmente conectados
     * @return true si currentOnline >= capacity
     */
    public boolean isFull(int currentOnline) {
        return currentOnline >= capacity;
    }

    /**
     * Valida si puede aceptar una nueva conexión
     *
     * @param currentOnline usuarios actualmente conectados
     * @return true si la sala acepta conexiones y no está llena
     */
    public boolean canAcceptConnection(int currentOnline) {
        return acceptsConnections() && !isFull(currentOnline);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
