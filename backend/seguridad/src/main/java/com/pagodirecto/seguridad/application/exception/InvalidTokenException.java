package com.pagodirecto.seguridad.application.exception;

/**
 * Excepción lanzada cuando un token JWT es inválido o ha expirado
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public class InvalidTokenException extends SecurityException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
