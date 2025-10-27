package com.pagodirecto.seguridad.application.service;

import com.pagodirecto.seguridad.application.dto.ChangePasswordRequest;
import com.pagodirecto.seguridad.application.dto.ProfileDTO;
import com.pagodirecto.seguridad.application.dto.UpdateProfileRequest;

import java.util.UUID;

/**
 * Service: ProfileService
 *
 * Servicio para gestión del perfil de usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface ProfileService {

    /**
     * Obtiene el perfil del usuario actual
     *
     * @param userId ID del usuario
     * @return ProfileDTO con la información del perfil
     */
    ProfileDTO getProfile(UUID userId);

    /**
     * Actualiza el perfil del usuario actual
     *
     * @param userId ID del usuario
     * @param request datos a actualizar
     * @return ProfileDTO actualizado
     */
    ProfileDTO updateProfile(UUID userId, UpdateProfileRequest request);

    /**
     * Cambia la contraseña del usuario
     *
     * @param userId ID del usuario
     * @param request datos de cambio de contraseña
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * Habilita MFA para el usuario
     *
     * @param userId ID del usuario
     * @return Secret de MFA para configurar en el autenticador
     */
    String enableMFA(UUID userId);

    /**
     * Deshabilita MFA para el usuario
     *
     * @param userId ID del usuario
     */
    void disableMFA(UUID userId);
}
