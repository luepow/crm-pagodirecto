package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Atributo Personalizado de Sala
 *
 * Patrón EAV (Entity-Attribute-Value) para atributos extensibles de salas.
 * Permite agregar metadatos personalizados sin cambios de esquema.
 * Tabla: dat_spd_room_attr
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "dat_spd_room_attr", indexes = {
    @Index(name = "idx_dat_spd_room_attr_room", columnList = "room_id"),
    @Index(name = "idx_dat_spd_room_attr_key", columnList = "attr_key")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_dat_spd_room_attr_key", columnNames = {"room_id", "attr_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAttr {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "attr_key", nullable = false, length = 100)
    private String attrKey;

    @Column(name = "attr_value", nullable = false, columnDefinition = "TEXT")
    private String attrValue;

    @Column(name = "data_type", nullable = false, length = 20)
    @Builder.Default
    private String dataType = "STRING";

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

    /**
     * Convierte el valor al tipo de dato especificado
     *
     * @return valor convertido como Object
     */
    public Object getTypedValue() {
        if (attrValue == null) {
            return null;
        }

        return switch (dataType.toUpperCase()) {
            case "INTEGER" -> Integer.parseInt(attrValue);
            case "FLOAT" -> Double.parseDouble(attrValue);
            case "BOOLEAN" -> Boolean.parseBoolean(attrValue);
            case "JSON", "STRING" -> attrValue;
            case "DATE" -> Instant.parse(attrValue);
            default -> attrValue;
        };
    }

    /**
     * Establece el valor desde un objeto tipado
     *
     * @param value valor a almacenar
     */
    public void setTypedValue(Object value) {
        if (value == null) {
            this.attrValue = null;
            return;
        }

        this.attrValue = value.toString();

        // Infer data type if not set
        if (dataType == null || "STRING".equals(dataType)) {
            if (value instanceof Integer || value instanceof Long) {
                this.dataType = "INTEGER";
            } else if (value instanceof Double || value instanceof Float) {
                this.dataType = "FLOAT";
            } else if (value instanceof Boolean) {
                this.dataType = "BOOLEAN";
            } else if (value instanceof Instant) {
                this.dataType = "DATE";
            } else {
                this.dataType = "STRING";
            }
        }

        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
