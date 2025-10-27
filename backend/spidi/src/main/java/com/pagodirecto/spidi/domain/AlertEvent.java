package com.pagodirecto.spidi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Evento de Alerta
 *
 * Historial de alertas disparadas (append-only para auditoría).
 * Registra alertas generadas por reglas de monitoreo.
 * Tabla: dat_spd_alert_event
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "dat_spd_alert_event", indexes = {
    @Index(name = "idx_dat_spd_alert_event_rule", columnList = "alert_rule_id"),
    @Index(name = "idx_dat_spd_alert_event_room", columnList = "room_id"),
    @Index(name = "idx_dat_spd_alert_event_created", columnList = "created_at"),
    @Index(name = "idx_dat_spd_alert_event_unack", columnList = "acknowledged"),
    @Index(name = "idx_dat_spd_alert_event_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_rule_id", nullable = false)
    private AlertRule alertRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "threshold_value", precision = 15, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "acknowledged", nullable = false)
    @Builder.Default
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Verifica si la alerta ha sido reconocida
     *
     * @return true si acknowledged = true
     */
    public boolean isAcknowledged() {
        return Boolean.TRUE.equals(this.acknowledged);
    }

    /**
     * Marca la alerta como reconocida
     *
     * @param userId ID del usuario que reconoce la alerta
     */
    public void acknowledge(UUID userId) {
        if (!isAcknowledged()) {
            this.acknowledged = true;
            this.acknowledgedAt = Instant.now();
            this.acknowledgedBy = userId;
        }
    }

    /**
     * Verifica si la alerta es crítica
     *
     * @return true si severity = CRITICAL
     */
    public boolean isCritical() {
        return AlertSeverity.CRITICAL.equals(this.severity);
    }

    /**
     * Verifica si la alerta requiere atención (WARNING o superior)
     *
     * @return true si severity >= WARNING
     */
    public boolean requiresAttention() {
        return severity == AlertSeverity.WARNING ||
               severity == AlertSeverity.ERROR ||
               severity == AlertSeverity.CRITICAL;
    }

    /**
     * Calcula el tiempo transcurrido desde que se generó la alerta
     *
     * @return segundos desde created_at
     */
    public long getAgeSeconds() {
        return Instant.now().getEpochSecond() - createdAt.getEpochSecond();
    }

    /**
     * Genera un mensaje formateado para notificaciones
     *
     * @return mensaje formateado
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ");
        sb.append(message);

        if (currentValue != null && thresholdValue != null) {
            sb.append(" (Current: ").append(currentValue)
              .append(", Threshold: ").append(thresholdValue).append(")");
        }

        return sb.toString();
    }
}
