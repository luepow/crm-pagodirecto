package com.pagodirecto.configuracion.application.service.impl;

import com.pagodirecto.configuracion.application.dto.*;
import com.pagodirecto.configuracion.application.service.ConfiguracionService;
import com.pagodirecto.configuracion.domain.Configuracion;
import com.pagodirecto.configuracion.domain.ConfiguracionCategoria;
import com.pagodirecto.configuracion.domain.ConfiguracionTipoDato;
import com.pagodirecto.configuracion.infrastructure.repository.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation: ConfiguracionServiceImpl
 *
 * Implementación del servicio de configuraciones
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionDTO> obtenerTodas() {
        log.debug("Obteniendo todas las configuraciones");
        return configuracionRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionDTO obtenerPorClave(String clave) {
        log.debug("Obteniendo configuración por clave: {}", clave);
        return configuracionRepository.findByClave(clave)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("Configuración no encontrada: " + clave));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionDTO> obtenerPorCategoria(ConfiguracionCategoria categoria) {
        log.debug("Obteniendo configuraciones por categoría: {}", categoria);
        return configuracionRepository.findByCategoria(categoria).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ConfiguracionDTO crear(ConfiguracionDTO dto, UUID userId) {
        log.info("Creando nueva configuración: {}", dto.getClave());

        if (configuracionRepository.existsByClave(dto.getClave())) {
            throw new IllegalArgumentException("Ya existe una configuración con la clave: " + dto.getClave());
        }

        Configuracion configuracion = Configuracion.builder()
            .clave(dto.getClave())
            .valor(dto.getValor())
            .categoria(dto.getCategoria())
            .tipoDato(dto.getTipoDato())
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .valorPorDefecto(dto.getValorPorDefecto())
            .esPublica(dto.getEsPublica() != null ? dto.getEsPublica() : false)
            .esModificable(dto.getEsModificable() != null ? dto.getEsModificable() : true)
            .validacionRegex(dto.getValidacionRegex())
            .createdBy(userId)
            .updatedBy(userId)
            .build();

        configuracion = configuracionRepository.save(configuracion);
        log.info("Configuración creada exitosamente con ID: {}", configuracion.getId());
        return toDTO(configuracion);
    }

    @Override
    public ConfiguracionDTO actualizar(UUID id, ConfiguracionDTO dto, UUID userId) {
        log.info("Actualizando configuración con ID: {}", id);

        Configuracion configuracion = configuracionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuración no encontrada con ID: " + id));

        if (!configuracion.getEsModificable()) {
            throw new IllegalStateException("La configuración no es modificable");
        }

        configuracion.setValor(dto.getValor());
        configuracion.setNombre(dto.getNombre());
        configuracion.setDescripcion(dto.getDescripcion());
        configuracion.setUpdatedBy(userId);
        configuracion.setUpdatedAt(Instant.now());

        configuracion = configuracionRepository.save(configuracion);
        log.info("Configuración actualizada exitosamente");
        return toDTO(configuracion);
    }

    @Override
    public ConfiguracionDTO actualizarValor(String clave, String valor, UUID userId) {
        log.info("Actualizando valor de configuración: {}", clave);

        Configuracion configuracion = configuracionRepository.findByClave(clave)
            .orElseThrow(() -> new RuntimeException("Configuración no encontrada: " + clave));

        if (!configuracion.getEsModificable()) {
            throw new IllegalStateException("La configuración no es modificable");
        }

        if (!configuracion.validarValor(valor)) {
            throw new IllegalArgumentException("El valor no cumple con las reglas de validación");
        }

        configuracion.setValor(valor);
        configuracion.setUpdatedBy(userId);
        configuracion.setUpdatedAt(Instant.now());

        configuracion = configuracionRepository.save(configuracion);
        log.info("Valor actualizado exitosamente");
        return toDTO(configuracion);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando configuración con ID: {}", id);

        Configuracion configuracion = configuracionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuración no encontrada con ID: " + id));

        if (!configuracion.getEsModificable()) {
            throw new IllegalStateException("La configuración no puede ser eliminada");
        }

        configuracionRepository.delete(configuracion);
        log.info("Configuración eliminada exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionGeneralDTO obtenerConfiguracionGeneral() {
        log.debug("Obteniendo configuración general");

        return ConfiguracionGeneralDTO.builder()
            .nombreEmpresa(getValor("general.nombre_empresa", "PagoDirecto CRM"))
            .logoUrl(getValor("general.logo_url", ""))
            .zonaHoraria(getValor("general.zona_horaria", "America/Mexico_City"))
            .moneda(getValor("general.moneda", "MXN"))
            .idioma(getValor("general.idioma", "es-MX"))
            .formatoFecha(getValor("general.formato_fecha", "dd/MM/yyyy"))
            .formatoHora(getValor("general.formato_hora", "HH:mm:ss"))
            .telefonoContacto(getValor("general.telefono_contacto", ""))
            .emailContacto(getValor("general.email_contacto", ""))
            .direccion(getValor("general.direccion", ""))
            .build();
    }

    @Override
    public ConfiguracionGeneralDTO actualizarConfiguracionGeneral(ConfiguracionGeneralDTO dto, UUID userId) {
        log.info("Actualizando configuración general");

        setValor("general.nombre_empresa", dto.getNombreEmpresa(), userId);
        setValor("general.logo_url", dto.getLogoUrl(), userId);
        setValor("general.zona_horaria", dto.getZonaHoraria(), userId);
        setValor("general.moneda", dto.getMoneda(), userId);
        setValor("general.idioma", dto.getIdioma(), userId);
        setValor("general.formato_fecha", dto.getFormatoFecha(), userId);
        setValor("general.formato_hora", dto.getFormatoHora(), userId);
        setValor("general.telefono_contacto", dto.getTelefonoContacto(), userId);
        setValor("general.email_contacto", dto.getEmailContacto(), userId);
        setValor("general.direccion", dto.getDireccion(), userId);

        log.info("Configuración general actualizada exitosamente");
        return obtenerConfiguracionGeneral();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionNotificacionesDTO obtenerConfiguracionNotificaciones() {
        log.debug("Obteniendo configuración de notificaciones");

        return ConfiguracionNotificacionesDTO.builder()
            .emailHabilitado(getBooleanValor("notif.email_habilitado", true))
            .smtpHost(getValor("notif.smtp_host", "smtp.gmail.com"))
            .smtpPort(getIntegerValor("notif.smtp_port", 587))
            .smtpUsername(getValor("notif.smtp_username", ""))
            .smtpTls(getBooleanValor("notif.smtp_tls", true))
            .emailFrom(getValor("notif.email_from", "noreply@pagodirecto.com"))
            .emailFromName(getValor("notif.email_from_name", "PagoDirecto CRM"))
            .pushHabilitado(getBooleanValor("notif.push_habilitado", false))
            .fcmApiKey(getValor("notif.fcm_api_key", ""))
            .smsHabilitado(getBooleanValor("notif.sms_habilitado", false))
            .smsProveedor(getValor("notif.sms_proveedor", "twilio"))
            .smsAccountSid(getValor("notif.sms_account_sid", ""))
            .smsFrom(getValor("notif.sms_from", ""))
            .notificarNuevosClientes(getBooleanValor("notif.nuevos_clientes", true))
            .notificarNuevasOportunidades(getBooleanValor("notif.nuevas_oportunidades", true))
            .notificarTareasVencidas(getBooleanValor("notif.tareas_vencidas", true))
            .notificarNuevasVentas(getBooleanValor("notif.nuevas_ventas", true))
            .build();
    }

    @Override
    public ConfiguracionNotificacionesDTO actualizarConfiguracionNotificaciones(
            ConfiguracionNotificacionesDTO dto, UUID userId) {
        log.info("Actualizando configuración de notificaciones");

        setValor("notif.email_habilitado", dto.getEmailHabilitado(), userId);
        setValor("notif.smtp_host", dto.getSmtpHost(), userId);
        setValor("notif.smtp_port", dto.getSmtpPort(), userId);
        setValor("notif.smtp_username", dto.getSmtpUsername(), userId);
        setValor("notif.smtp_tls", dto.getSmtpTls(), userId);
        setValor("notif.email_from", dto.getEmailFrom(), userId);
        setValor("notif.email_from_name", dto.getEmailFromName(), userId);
        setValor("notif.push_habilitado", dto.getPushHabilitado(), userId);
        setValor("notif.fcm_api_key", dto.getFcmApiKey(), userId);
        setValor("notif.sms_habilitado", dto.getSmsHabilitado(), userId);
        setValor("notif.sms_proveedor", dto.getSmsProveedor(), userId);
        setValor("notif.sms_account_sid", dto.getSmsAccountSid(), userId);
        setValor("notif.sms_from", dto.getSmsFrom(), userId);
        setValor("notif.nuevos_clientes", dto.getNotificarNuevosClientes(), userId);
        setValor("notif.nuevas_oportunidades", dto.getNotificarNuevasOportunidades(), userId);
        setValor("notif.tareas_vencidas", dto.getNotificarTareasVencidas(), userId);
        setValor("notif.nuevas_ventas", dto.getNotificarNuevasVentas(), userId);

        log.info("Configuración de notificaciones actualizada exitosamente");
        return obtenerConfiguracionNotificaciones();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionIntegracionesDTO obtenerConfiguracionIntegraciones() {
        log.debug("Obteniendo configuración de integraciones");

        return ConfiguracionIntegracionesDTO.builder()
            .googleHabilitado(getBooleanValor("integ.google_habilitado", false))
            .googleClientId(getValor("integ.google_client_id", ""))
            .googleCalendarHabilitado(getBooleanValor("integ.google_calendar", false))
            .pagosPasarelaHabilitada(getBooleanValor("integ.pagos_habilitado", false))
            .pagosProveedor(getValor("integ.pagos_proveedor", "stripe"))
            .pagosApiKey(getValor("integ.pagos_api_key", ""))
            .pagosWebhookUrl(getValor("integ.pagos_webhook_url", ""))
            .webhooksHabilitado(getBooleanValor("integ.webhooks_habilitado", false))
            .webhookClientesUrl(getValor("integ.webhook_clientes_url", ""))
            .webhookOportunidadesUrl(getValor("integ.webhook_oportunidades_url", ""))
            .webhookVentasUrl(getValor("integ.webhook_ventas_url", ""))
            .webhookSecret(getValor("integ.webhook_secret", ""))
            .storageProveedor(getValor("integ.storage_proveedor", "local"))
            .s3BucketName(getValor("integ.s3_bucket_name", ""))
            .s3Region(getValor("integ.s3_region", "us-east-1"))
            .apiExternaUrl(getValor("integ.api_externa_url", ""))
            .apiExternaKey(getValor("integ.api_externa_key", ""))
            .apiTimeout(getIntegerValor("integ.api_timeout", 30))
            .build();
    }

    @Override
    public ConfiguracionIntegracionesDTO actualizarConfiguracionIntegraciones(
            ConfiguracionIntegracionesDTO dto, UUID userId) {
        log.info("Actualizando configuración de integraciones");

        setValor("integ.google_habilitado", dto.getGoogleHabilitado(), userId);
        setValor("integ.google_client_id", dto.getGoogleClientId(), userId);
        setValor("integ.google_calendar", dto.getGoogleCalendarHabilitado(), userId);
        setValor("integ.pagos_habilitado", dto.getPagosPasarelaHabilitada(), userId);
        setValor("integ.pagos_proveedor", dto.getPagosProveedor(), userId);
        setValor("integ.pagos_api_key", dto.getPagosApiKey(), userId);
        setValor("integ.pagos_webhook_url", dto.getPagosWebhookUrl(), userId);
        setValor("integ.webhooks_habilitado", dto.getWebhooksHabilitado(), userId);
        setValor("integ.webhook_clientes_url", dto.getWebhookClientesUrl(), userId);
        setValor("integ.webhook_oportunidades_url", dto.getWebhookOportunidadesUrl(), userId);
        setValor("integ.webhook_ventas_url", dto.getWebhookVentasUrl(), userId);
        setValor("integ.webhook_secret", dto.getWebhookSecret(), userId);
        setValor("integ.storage_proveedor", dto.getStorageProveedor(), userId);
        setValor("integ.s3_bucket_name", dto.getS3BucketName(), userId);
        setValor("integ.s3_region", dto.getS3Region(), userId);
        setValor("integ.api_externa_url", dto.getApiExternaUrl(), userId);
        setValor("integ.api_externa_key", dto.getApiExternaKey(), userId);
        setValor("integ.api_timeout", dto.getApiTimeout(), userId);

        log.info("Configuración de integraciones actualizada exitosamente");
        return obtenerConfiguracionIntegraciones();
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionSeguridadDTO obtenerConfiguracionSeguridad() {
        log.debug("Obteniendo configuración de seguridad");

        return ConfiguracionSeguridadDTO.builder()
            .passwordMinLength(getIntegerValor("seg.password_min_length", 8))
            .passwordRequiereMaxusculas(getBooleanValor("seg.password_req_mayusculas", true))
            .passwordRequiereMinusculas(getBooleanValor("seg.password_req_minusculas", true))
            .passwordRequiereNumeros(getBooleanValor("seg.password_req_numeros", true))
            .passwordRequiereEspeciales(getBooleanValor("seg.password_req_especiales", true))
            .passwordDiasExpiracion(getIntegerValor("seg.password_dias_expiracion", 90))
            .passwordHistorial(getIntegerValor("seg.password_historial", 5))
            .sessionDuracion(getIntegerValor("seg.session_duracion", 60))
            .sessionTimeoutInactividad(getIntegerValor("seg.session_timeout_inactividad", 30))
            .sessionMaxSimultaneas(getIntegerValor("seg.session_max_simultaneas", 3))
            .loginMaxIntentosFallidos(getIntegerValor("seg.login_max_intentos", 5))
            .loginDuracionBloqueo(getIntegerValor("seg.login_duracion_bloqueo", 30))
            .mfaObligatorio(getBooleanValor("seg.mfa_obligatorio", false))
            .mfaObligatorioAdmins(getBooleanValor("seg.mfa_obligatorio_admins", true))
            .ipRestriccionHabilitada(getBooleanValor("seg.ip_restriccion_habilitada", false))
            .ipListaPermitidas(getValor("seg.ip_lista_permitidas", ""))
            .auditHabilitado(getBooleanValor("seg.audit_habilitado", true))
            .auditRetencionDias(getIntegerValor("seg.audit_retencion_dias", 365))
            .auditDatosSensibles(getBooleanValor("seg.audit_datos_sensibles", true))
            .corsOrigenes(getValor("seg.cors_origenes", "*"))
            .rateLimitHabilitado(getBooleanValor("seg.rate_limit_habilitado", true))
            .rateLimitMaxRequests(getIntegerValor("seg.rate_limit_max_requests", 100))
            .build();
    }

    @Override
    public ConfiguracionSeguridadDTO actualizarConfiguracionSeguridad(
            ConfiguracionSeguridadDTO dto, UUID userId) {
        log.info("Actualizando configuración de seguridad");

        setValor("seg.password_min_length", dto.getPasswordMinLength(), userId);
        setValor("seg.password_req_mayusculas", dto.getPasswordRequiereMaxusculas(), userId);
        setValor("seg.password_req_minusculas", dto.getPasswordRequiereMinusculas(), userId);
        setValor("seg.password_req_numeros", dto.getPasswordRequiereNumeros(), userId);
        setValor("seg.password_req_especiales", dto.getPasswordRequiereEspeciales(), userId);
        setValor("seg.password_dias_expiracion", dto.getPasswordDiasExpiracion(), userId);
        setValor("seg.password_historial", dto.getPasswordHistorial(), userId);
        setValor("seg.session_duracion", dto.getSessionDuracion(), userId);
        setValor("seg.session_timeout_inactividad", dto.getSessionTimeoutInactividad(), userId);
        setValor("seg.session_max_simultaneas", dto.getSessionMaxSimultaneas(), userId);
        setValor("seg.login_max_intentos", dto.getLoginMaxIntentosFallidos(), userId);
        setValor("seg.login_duracion_bloqueo", dto.getLoginDuracionBloqueo(), userId);
        setValor("seg.mfa_obligatorio", dto.getMfaObligatorio(), userId);
        setValor("seg.mfa_obligatorio_admins", dto.getMfaObligatorioAdmins(), userId);
        setValor("seg.ip_restriccion_habilitada", dto.getIpRestriccionHabilitada(), userId);
        setValor("seg.ip_lista_permitidas", dto.getIpListaPermitidas(), userId);
        setValor("seg.audit_habilitado", dto.getAuditHabilitado(), userId);
        setValor("seg.audit_retencion_dias", dto.getAuditRetencionDias(), userId);
        setValor("seg.audit_datos_sensibles", dto.getAuditDatosSensibles(), userId);
        setValor("seg.cors_origenes", dto.getCorsOrigenes(), userId);
        setValor("seg.rate_limit_habilitado", dto.getRateLimitHabilitado(), userId);
        setValor("seg.rate_limit_max_requests", dto.getRateLimitMaxRequests(), userId);

        log.info("Configuración de seguridad actualizada exitosamente");
        return obtenerConfiguracionSeguridad();
    }

    @Override
    public void inicializarConfiguracionesPorDefecto() {
        log.info("Inicializando configuraciones por defecto");
        // La inicialización se realiza mediante migración SQL
        log.info("Configuraciones por defecto inicializadas");
    }

    // Helper methods

    private String getValor(String clave, String valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
            .map(Configuracion::getValorString)
            .orElse(valorPorDefecto);
    }

    private Integer getIntegerValor(String clave, Integer valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
            .map(config -> {
                try {
                    return config.getValorInteger();
                } catch (Exception e) {
                    return valorPorDefecto;
                }
            })
            .orElse(valorPorDefecto);
    }

    private Boolean getBooleanValor(String clave, Boolean valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
            .map(config -> {
                try {
                    return config.getValorBoolean();
                } catch (Exception e) {
                    return valorPorDefecto;
                }
            })
            .orElse(valorPorDefecto);
    }

    private void setValor(String clave, Object valor, UUID userId) {
        if (valor == null) return;

        configuracionRepository.findByClave(clave).ifPresent(config -> {
            config.setValor(String.valueOf(valor));
            config.setUpdatedBy(userId);
            config.setUpdatedAt(Instant.now());
            configuracionRepository.save(config);
        });
    }

    private ConfiguracionDTO toDTO(Configuracion config) {
        return ConfiguracionDTO.builder()
            .id(config.getId())
            .unidadNegocioId(config.getUnidadNegocioId())
            .clave(config.getClave())
            .valor(config.getValor())
            .categoria(config.getCategoria())
            .tipoDato(config.getTipoDato())
            .nombre(config.getNombre())
            .descripcion(config.getDescripcion())
            .valorPorDefecto(config.getValorPorDefecto())
            .esPublica(config.getEsPublica())
            .esModificable(config.getEsModificable())
            .validacionRegex(config.getValidacionRegex())
            .createdAt(config.getCreatedAt())
            .updatedAt(config.getUpdatedAt())
            .build();
    }
}
