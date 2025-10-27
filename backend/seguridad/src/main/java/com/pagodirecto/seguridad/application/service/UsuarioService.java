package com.pagodirecto.seguridad.application.service;

import com.pagodirecto.seguridad.application.dto.CreateUsuarioRequest;
import com.pagodirecto.seguridad.application.dto.UpdateUsuarioRequest;
import com.pagodirecto.seguridad.application.dto.UsuarioDTO;

import java.util.List;
import java.util.UUID;

/**
 * Servicio: UsuarioService
 *
 * Servicio de gestión CRUD de usuarios
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface UsuarioService {

    /**
     * Obtiene todos los usuarios
     *
     * @return lista de usuarios
     */
    List<UsuarioDTO> getAllUsuarios();

    /**
     * Obtiene usuarios por unidad de negocio
     *
     * @param unidadNegocioId el ID de la unidad de negocio
     * @return lista de usuarios
     */
    List<UsuarioDTO> getUsuariosByUnidadNegocio(UUID unidadNegocioId);

    /**
     * Obtiene un usuario por su ID
     *
     * @param id el ID del usuario
     * @return el usuario
     */
    UsuarioDTO getUsuarioById(UUID id);

    /**
     * Crea un nuevo usuario
     *
     * @param request datos del nuevo usuario
     * @param creatorId ID del usuario que crea
     * @return el usuario creado
     */
    UsuarioDTO createUsuario(CreateUsuarioRequest request, UUID creatorId);

    /**
     * Actualiza un usuario existente
     *
     * @param id el ID del usuario
     * @param request datos actualizados
     * @param updaterId ID del usuario que actualiza
     * @return el usuario actualizado
     */
    UsuarioDTO updateUsuario(UUID id, UpdateUsuarioRequest request, UUID updaterId);

    /**
     * Elimina un usuario (soft delete)
     *
     * @param id el ID del usuario
     */
    void deleteUsuario(UUID id);

    /**
     * Bloquea un usuario
     *
     * @param id el ID del usuario
     * @param duracionSegundos duración del bloqueo en segundos (null = indefinido)
     */
    void bloquearUsuario(UUID id, Integer duracionSegundos);

    /**
     * Desbloquea un usuario
     *
     * @param id el ID del usuario
     */
    void desbloquearUsuario(UUID id);

    /**
     * Restablece la contraseña de un usuario
     *
     * @param id el ID del usuario
     * @param newPassword la nueva contraseña
     */
    void resetPassword(UUID id, String newPassword);
}
