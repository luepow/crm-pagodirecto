package com.pagodirecto.seguridad.application.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public class UserNotFoundException extends SecurityException {

    public UserNotFoundException(String username) {
        super(String.format("Usuario no encontrado: %s", username));
    }

    public UserNotFoundException(UUID userId) {
        super(String.format("Usuario no encontrado con ID: %s", userId));
    }
}
