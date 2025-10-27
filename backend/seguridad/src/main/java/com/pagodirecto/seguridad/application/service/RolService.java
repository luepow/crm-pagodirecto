package com.pagodirecto.seguridad.application.service;

import com.pagodirecto.seguridad.application.dto.CreateRolRequest;
import com.pagodirecto.seguridad.application.dto.RolWithPermisosDTO;
import com.pagodirecto.seguridad.application.dto.UpdateRolRequest;

import java.util.List;
import java.util.UUID;

/**
 * Servicio: RolService
 *
 * Servicio de gestión CRUD de roles y asignación de permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface RolService {

    /**
     * Obtiene todos los roles
     */
    List<RolWithPermisosDTO> getAllRoles();

    /**
     * Obtiene roles por unidad de negocio
     */
    List<RolWithPermisosDTO> getRolesByUnidadNegocio(UUID unidadNegocioId);

    /**
     * Obtiene roles por departamento
     */
    List<RolWithPermisosDTO> getRolesByDepartamento(String departamento);

    /**
     * Obtiene un rol por su ID con permisos
     */
    RolWithPermisosDTO getRolById(UUID id);

    /**
     * Crea un nuevo rol
     */
    RolWithPermisosDTO createRol(CreateRolRequest request, UUID creatorId);

    /**
     * Actualiza un rol existente
     */
    RolWithPermisosDTO updateRol(UUID id, UpdateRolRequest request, UUID updaterId);

    /**
     * Elimina un rol (soft delete)
     */
    void deleteRol(UUID id);

    /**
     * Asigna permisos a un rol
     */
    void assignPermisosToRol(UUID rolId, List<UUID> permisoIds);

    /**
     * Remueve permisos de un rol
     */
    void removePermisosFromRol(UUID rolId, List<UUID> permisoIds);
}
