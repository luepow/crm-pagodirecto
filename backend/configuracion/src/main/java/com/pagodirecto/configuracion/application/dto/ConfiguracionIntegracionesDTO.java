package com.pagodirecto.configuracion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ConfiguracionIntegracionesDTO
 *
 * Configuración de integraciones con servicios externos
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Configuración de integraciones")
public class ConfiguracionIntegracionesDTO {

    // Google integrations
    @Schema(description = "Habilitar integración con Google", example = "false")
    private Boolean googleHabilitado;

    @Schema(description = "Google Client ID")
    private String googleClientId;

    @Schema(description = "Google Calendar API habilitado", example = "false")
    private Boolean googleCalendarHabilitado;

    // Payment gateways
    @Schema(description = "Habilitar pasarela de pagos", example = "false")
    private Boolean pagosPasarelaHabilitada;

    @Schema(description = "Proveedor de pagos", example = "stripe")
    private String pagosProveedor;

    @Schema(description = "API Key del proveedor de pagos")
    private String pagosApiKey;

    @Schema(description = "Webhook URL para notificaciones de pago")
    private String pagosWebhookUrl;

    // Webhooks
    @Schema(description = "Habilitar webhooks", example = "false")
    private Boolean webhooksHabilitado;

    @Schema(description = "URL del webhook para clientes nuevos")
    private String webhookClientesUrl;

    @Schema(description = "URL del webhook para oportunidades")
    private String webhookOportunidadesUrl;

    @Schema(description = "URL del webhook para ventas")
    private String webhookVentasUrl;

    @Schema(description = "Secret para firma de webhooks")
    private String webhookSecret;

    // Storage
    @Schema(description = "Proveedor de almacenamiento", example = "local")
    private String storageProveedor;

    @Schema(description = "AWS S3 Bucket name")
    private String s3BucketName;

    @Schema(description = "AWS S3 Region", example = "us-east-1")
    private String s3Region;

    // API external
    @Schema(description = "URL de API externa")
    private String apiExternaUrl;

    @Schema(description = "API Key externa")
    private String apiExternaKey;

    @Schema(description = "Timeout para llamadas API (segundos)", example = "30")
    private Integer apiTimeout;
}
