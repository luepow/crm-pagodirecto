package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Regla de Alerta
 *
 * Define reglas de monitoreo y alertas para salas Spidi.
 * Soporta expresiones personalizadas para condiciones complejas.
 * Tabla: dat_spd_alert_rule
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "dat_spd_alert_rule", indexes = {
    @Index(name = "idx_dat_spd_alert_rule_room", columnList = "room_id"),
    @Index(name = "idx_dat_spd_alert_rule_enabled", columnList = "enabled"),
    @Index(name = "idx_dat_spd_alert_rule_unidad_negocio", columnList = "unidad_negocio_id")
})
@SQLDelete(sql = "UPDATE dat_spd_alert_rule SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room; // NULL = aplica a todas las salas

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private AlertRuleType ruleType;

    @Column(name = "condition_expression", nullable = false, columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "threshold_value", precision = 15, scale = 2)
    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private AlertSeverity severity = AlertSeverity.WARNING;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "rate_limit_minutes")
    @Builder.Default
    private Integer rateLimitMinutes = 5;

    @Column(name = "notification_channels", columnDefinition = "jsonb")
    private String notificationChannels;

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
     * Verifica si la regla está activa y habilitada
     *
     * @return true si enabled = true
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.enabled);
    }

    /**
     * Habilita la regla
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Deshabilita la regla
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Verifica si la regla aplica a una sala específica o a todas
     *
     * @return true si room_id es NULL (aplica a todas)
     */
    public boolean appliesToAllRooms() {
        return this.room == null;
    }

    /**
     * Verifica si la regla aplica a una sala específica
     *
     * @param roomId ID de la sala a verificar
     * @return true si la regla aplica a esta sala
     */
    public boolean appliesTo(UUID roomId) {
        if (appliesToAllRooms()) {
            return true;
        }
        return this.room != null && this.room.getId().equals(roomId);
    }

    /**
     * Evalúa la expresión de condición con valores proporcionados
     * Nota: Esta es una versión simplificada. En producción, usar Groovy Engine.
     *
     * @param onlineCount usuarios online actuales
     * @param capacity capacidad de la sala
     * @param avgLatency latencia promedio
     * @return true si la condición se cumple
     */
    public boolean evaluateCondition(int onlineCount, int capacity, Integer avgLatency) {
        return switch (ruleType) {
            case CAPACITY -> {
                double percent = (onlineCount * 100.0) / capacity;
                yield thresholdValue != null && percent >= thresholdValue.doubleValue();
            }
            case LATENCY -> avgLatency != null && thresholdValue != null &&
                    avgLatency >= thresholdValue.intValue();
            case HEARTBEAT -> {
                // Para HEARTBEAT, la evaluación se hace en el servicio
                // basado en last_heartbeat_at
                yield false;
            }
            case CUSTOM -> {
                // Para expresiones personalizadas, usar Groovy evaluator
                // TODO: Implementar Groovy script evaluation
                yield false;
            }
        };
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
