package com.pagodirecto.seguridad.application.exception;

/**
 * Excepción base para errores de seguridad
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public class SecurityException extends RuntimeException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
