package com.pagodirecto.configuracion.application.service;

import com.pagodirecto.configuracion.application.dto.*;
import com.pagodirecto.configuracion.domain.ConfiguracionCategoria;

import java.util.List;
import java.util.UUID;

/**
 * Service: ConfiguracionService
 *
 * Servicio para gestión de configuraciones del sistema
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface ConfiguracionService {

    /**
     * Obtiene todas las configuraciones
     */
    List<ConfiguracionDTO> obtenerTodas();

    /**
     * Obtiene una configuración por clave
     */
    ConfiguracionDTO obtenerPorClave(String clave);

    /**
     * Obtiene todas las configuraciones de una categoría
     */
    List<ConfiguracionDTO> obtenerPorCategoria(ConfiguracionCategoria categoria);

    /**
     * Crea una nueva configuración
     */
    ConfiguracionDTO crear(ConfiguracionDTO configuracionDTO, UUID userId);

    /**
     * Actualiza una configuración existente
     */
    ConfiguracionDTO actualizar(UUID id, ConfiguracionDTO configuracionDTO, UUID userId);

    /**
     * Actualiza el valor de una configuración por clave
     */
    ConfiguracionDTO actualizarValor(String clave, String valor, UUID userId);

    /**
     * Elimina una configuración
     */
    void eliminar(UUID id);

    /**
     * Obtiene la configuración general
     */
    ConfiguracionGeneralDTO obtenerConfiguracionGeneral();

    /**
     * Actualiza la configuración general
     */
    ConfiguracionGeneralDTO actualizarConfiguracionGeneral(ConfiguracionGeneralDTO dto, UUID userId);

    /**
     * Obtiene la configuración de notificaciones
     */
    ConfiguracionNotificacionesDTO obtenerConfiguracionNotificaciones();

    /**
     * Actualiza la configuración de notificaciones
     */
    ConfiguracionNotificacionesDTO actualizarConfiguracionNotificaciones(
        ConfiguracionNotificacionesDTO dto, UUID userId);

    /**
     * Obtiene la configuración de integraciones
     */
    ConfiguracionIntegracionesDTO obtenerConfiguracionIntegraciones();

    /**
     * Actualiza la configuración de integraciones
     */
    ConfiguracionIntegracionesDTO actualizarConfiguracionIntegraciones(
        ConfiguracionIntegracionesDTO dto, UUID userId);

    /**
     * Obtiene la configuración de seguridad
     */
    ConfiguracionSeguridadDTO obtenerConfiguracionSeguridad();

    /**
     * Actualiza la configuración de seguridad
     */
    ConfiguracionSeguridadDTO actualizarConfiguracionSeguridad(
        ConfiguracionSeguridadDTO dto, UUID userId);

    /**
     * Inicializa las configuraciones por defecto del sistema
     */
    void inicializarConfiguracionesPorDefecto();
}
