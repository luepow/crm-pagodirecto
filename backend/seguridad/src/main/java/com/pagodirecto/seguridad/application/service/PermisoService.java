package com.pagodirecto.seguridad.application.service;

import com.pagodirecto.seguridad.application.dto.CreatePermisoRequest;
import com.pagodirecto.seguridad.application.dto.PermisoDTO;
import com.pagodirecto.seguridad.application.dto.UpdatePermisoRequest;

import java.util.List;
import java.util.UUID;

/**
 * Servicio: PermisoService
 *
 * Servicio de gestión CRUD de permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface PermisoService {

    /**
     * Obtiene todos los permisos
     */
    List<PermisoDTO> getAllPermisos();

    /**
     * Obtiene permisos por recurso
     */
    List<PermisoDTO> getPermisosByRecurso(String recurso);

    /**
     * Obtiene permisos por acción
     */
    List<PermisoDTO> getPermisosByAccion(String accion);

    /**
     * Obtiene un permiso por su ID
     */
    PermisoDTO getPermisoById(UUID id);

    /**
     * Crea un nuevo permiso
     */
    PermisoDTO createPermiso(CreatePermisoRequest request, UUID creatorId);

    /**
     * Actualiza un permiso existente
     */
    PermisoDTO updatePermiso(UUID id, UpdatePermisoRequest request, UUID updaterId);

    /**
     * Elimina un permiso (soft delete)
     */
    void deletePermiso(UUID id);
}
