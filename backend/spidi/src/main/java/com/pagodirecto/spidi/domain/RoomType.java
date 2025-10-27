package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Tipo de Sala Spidi
 *
 * Catálogo de tipos de salas (WebRTC, Chat, Notification, etc.)
 * Tabla: tba_spd_room_type
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "tba_spd_room_type", indexes = {
    @Index(name = "idx_tba_spd_room_type_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "default_capacity", nullable = false)
    @Builder.Default
    private Integer defaultCapacity = 100;

    @Column(name = "default_ttl_seconds", nullable = false)
    @Builder.Default
    private Integer defaultTtlSeconds = 3600;

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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
