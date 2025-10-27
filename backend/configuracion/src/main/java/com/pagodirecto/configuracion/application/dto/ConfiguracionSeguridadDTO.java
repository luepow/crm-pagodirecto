package com.pagodirecto.configuracion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ConfiguracionSeguridadDTO
 *
 * Configuración de políticas de seguridad
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Configuración de seguridad")
public class ConfiguracionSeguridadDTO {

    // Password policies
    @Schema(description = "Longitud mínima de contraseña", example = "8")
    private Integer passwordMinLength;

    @Schema(description = "Requiere mayúsculas", example = "true")
    private Boolean passwordRequiereMaxusculas;

    @Schema(description = "Requiere minúsculas", example = "true")
    private Boolean passwordRequiereMinusculas;

    @Schema(description = "Requiere números", example = "true")
    private Boolean passwordRequiereNumeros;

    @Schema(description = "Requiere caracteres especiales", example = "true")
    private Boolean passwordRequiereEspeciales;

    @Schema(description = "Días de expiración de contraseña", example = "90")
    private Integer passwordDiasExpiracion;

    @Schema(description = "Historial de contraseñas (no repetir las últimas N)", example = "5")
    private Integer passwordHistorial;

    // Session and login
    @Schema(description = "Duración de sesión en minutos", example = "60")
    private Integer sessionDuracion;

    @Schema(description = "Timeout de inactividad en minutos", example = "30")
    private Integer sessionTimeoutInactividad;

    @Schema(description = "Máximo de sesiones simultáneas por usuario", example = "3")
    private Integer sessionMaxSimultaneas;

    @Schema(description = "Máximo de intentos de login fallidos", example = "5")
    private Integer loginMaxIntentosFallidos;

    @Schema(description = "Duración de bloqueo tras intentos fallidos (minutos)", example = "30")
    private Integer loginDuracionBloqueo;

    // MFA
    @Schema(description = "MFA obligatorio para todos los usuarios", example = "false")
    private Boolean mfaObligatorio;

    @Schema(description = "MFA obligatorio para administradores", example = "true")
    private Boolean mfaObligatorioAdmins;

    // IP restrictions
    @Schema(description = "Habilitar restricción por IP", example = "false")
    private Boolean ipRestriccionHabilitada;

    @Schema(description = "Lista de IPs permitidas (separadas por coma)")
    private String ipListaPermitidas;

    // Audit and logging
    @Schema(description = "Habilitar registro de auditoría", example = "true")
    private Boolean auditHabilitado;

    @Schema(description = "Retención de logs de auditoría (días)", example = "365")
    private Integer auditRetencionDias;

    @Schema(description = "Registrar acceso a datos sensibles", example = "true")
    private Boolean auditDatosSensibles;

    // CORS and API
    @Schema(description = "Orígenes CORS permitidos")
    private String corsOrigenes;

    @Schema(description = "Rate limiting habilitado", example = "true")
    private Boolean rateLimitHabilitado;

    @Schema(description = "Máximo de requests por minuto", example = "100")
    private Integer rateLimitMaxRequests;
}
