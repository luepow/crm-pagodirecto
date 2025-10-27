package com.pagodirecto.seguridad.application.exception;

/**
 * Excepción lanzada cuando falla la autenticación de un usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public class AuthenticationException extends SecurityException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
