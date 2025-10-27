package com.pagodirecto.configuracion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ConfiguracionNotificacionesDTO
 *
 * Configuración de notificaciones y alertas
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Configuración de notificaciones")
public class ConfiguracionNotificacionesDTO {

    // Email notifications
    @Schema(description = "Habilitar notificaciones por email", example = "true")
    private Boolean emailHabilitado;

    @Schema(description = "Servidor SMTP", example = "smtp.gmail.com")
    private String smtpHost;

    @Schema(description = "Puerto SMTP", example = "587")
    private Integer smtpPort;

    @Schema(description = "Usuario SMTP", example = "noreply@pagodirecto.com")
    private String smtpUsername;

    @Schema(description = "Usa TLS", example = "true")
    private Boolean smtpTls;

    @Schema(description = "Email remitente", example = "noreply@pagodirecto.com")
    private String emailFrom;

    @Schema(description = "Nombre del remitente", example = "PagoDirecto CRM")
    private String emailFromName;

    // Push notifications
    @Schema(description = "Habilitar notificaciones push", example = "false")
    private Boolean pushHabilitado;

    @Schema(description = "API key de Firebase Cloud Messaging")
    private String fcmApiKey;

    // SMS notifications
    @Schema(description = "Habilitar notificaciones SMS", example = "false")
    private Boolean smsHabilitado;

    @Schema(description = "Proveedor de SMS", example = "twilio")
    private String smsProveedor;

    @Schema(description = "Account SID (Twilio)")
    private String smsAccountSid;

    @Schema(description = "Número de teléfono remitente", example = "+525512345678")
    private String smsFrom;

    // Notification preferences
    @Schema(description = "Notificar nuevos clientes", example = "true")
    private Boolean notificarNuevosClientes;

    @Schema(description = "Notificar nuevas oportunidades", example = "true")
    private Boolean notificarNuevasOportunidades;

    @Schema(description = "Notificar tareas vencidas", example = "true")
    private Boolean notificarTareasVencidas;

    @Schema(description = "Notificar nuevas ventas", example = "true")
    private Boolean notificarNuevasVentas;
}
