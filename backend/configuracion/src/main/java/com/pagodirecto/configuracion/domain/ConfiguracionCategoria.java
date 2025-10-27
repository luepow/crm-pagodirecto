package com.pagodirecto.configuracion.domain;

/**
 * Enum: ConfiguracionCategoria
 *
 * Categorías de configuración del sistema
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum ConfiguracionCategoria {
    GENERAL("General", "Configuración general del sistema"),
    NOTIFICACIONES("Notificaciones", "Configuración de notificaciones y alertas"),
    INTEGRACIONES("Integraciones", "Configuración de APIs y servicios externos"),
    SEGURIDAD("Seguridad", "Políticas de seguridad y acceso");

    private final String nombre;
    private final String descripcion;

    ConfiguracionCategoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
