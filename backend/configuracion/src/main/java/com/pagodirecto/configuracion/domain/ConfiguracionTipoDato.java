package com.pagodirecto.configuracion.domain;

/**
 * Enum: ConfiguracionTipoDato
 *
 * Tipos de datos soportados para valores de configuración
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public enum ConfiguracionTipoDato {
    STRING("String", "Cadena de texto"),
    INTEGER("Integer", "Número entero"),
    BOOLEAN("Boolean", "Verdadero o falso"),
    DECIMAL("Decimal", "Número decimal"),
    JSON("JSON", "Objeto JSON"),
    URL("URL", "URL válida"),
    EMAIL("Email", "Correo electrónico"),
    PHONE("Phone", "Número telefónico");

    private final String nombre;
    private final String descripcion;

    ConfiguracionTipoDato(String nombre, String descripcion) {
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
