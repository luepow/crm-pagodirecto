package com.pagodirecto.seguridad.application.service.impl;

import com.pagodirecto.seguridad.application.dto.*;
import com.pagodirecto.seguridad.application.service.RolService;
import com.pagodirecto.seguridad.domain.Permiso;
import com.pagodirecto.seguridad.domain.Rol;
import com.pagodirecto.seguridad.infrastructure.repository.PermisoRepository;
import com.pagodirecto.seguridad.infrastructure.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación: RolServiceImpl
 *
 * Implementación del servicio de gestión CRUD de roles
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolWithPermisosDTO> getAllRoles() {
        log.info("Obteniendo todos los roles");
        return rolRepository.findAll().stream()
            .map(this::toRolWithPermisosDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolWithPermisosDTO> getRolesByUnidadNegocio(UUID unidadNegocioId) {
        log.info("Obteniendo roles por unidad de negocio: {}", unidadNegocioId);
        return rolRepository.findByUnidadNegocioId(unidadNegocioId).stream()
            .map(this::toRolWithPermisosDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolWithPermisosDTO> getRolesByDepartamento(String departamento) {
        log.info("Obteniendo roles por departamento: {}", departamento);
        return rolRepository.findByDepartamento(departamento).stream()
            .map(this::toRolWithPermisosDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolWithPermisosDTO getRolById(UUID id) {
        log.info("Obteniendo rol por ID: {}", id);
        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        return toRolWithPermisosDTO(rol);
    }

    @Override
    public RolWithPermisosDTO createRol(CreateRolRequest request, UUID creatorId) {
        log.info("Creando nuevo rol: {}", request.getNombre());

        // Crear el rol
        Rol rol = Rol.builder()
            .unidadNegocioId(request.getUnidadNegocioId())
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .departamento(request.getDepartamento())
            .nivelJerarquico(request.getNivelJerarquico() != null ? request.getNivelJerarquico() : 0)
            .createdAt(Instant.now())
            .createdBy(creatorId)
            .updatedAt(Instant.now())
            .updatedBy(creatorId)
            .build();

        // Asignar permisos si se proporcionaron
        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            Set<Permiso> permisos = new HashSet<>(permisoRepository.findAllById(request.getPermisoIds()));
            rol.setPermisos(permisos);
        }

        Rol savedRol = rolRepository.save(rol);
        log.info("Rol creado exitosamente: {}", savedRol.getId());

        return toRolWithPermisosDTO(savedRol);
    }

    @Override
    public RolWithPermisosDTO updateRol(UUID id, UpdateRolRequest request, UUID updaterId) {
        log.info("Actualizando rol: {}", id);

        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Actualizar datos
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());
        rol.setDepartamento(request.getDepartamento());
        
        if (request.getNivelJerarquico() != null) {
            rol.setNivelJerarquico(request.getNivelJerarquico());
        }

        rol.setUpdatedAt(Instant.now());
        rol.setUpdatedBy(updaterId);

        // Actualizar permisos si se proporcionaron
        if (request.getPermisoIds() != null) {
            Set<Permiso> permisos = new HashSet<>(permisoRepository.findAllById(request.getPermisoIds()));
            rol.setPermisos(permisos);
        }

        Rol updatedRol = rolRepository.save(rol);
        log.info("Rol actualizado exitosamente: {}", id);

        return toRolWithPermisosDTO(updatedRol);
    }

    @Override
    public void deleteRol(UUID id) {
        log.info("Eliminando rol (soft delete): {}", id);

        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // TODO: Verificar que no haya usuarios asignados a este rol
        
        rolRepository.delete(rol);
        log.info("Rol eliminado exitosamente: {}", id);
    }

    @Override
    public void assignPermisosToRol(UUID rolId, List<UUID> permisoIds) {
        log.info("Asignando {} permisos al rol {}", permisoIds.size(), rolId);

        Rol rol = rolRepository.findById(rolId)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));

        List<Permiso> permisos = permisoRepository.findAllById(permisoIds);
        rol.getPermisos().addAll(permisos);
        
        rol.setUpdatedAt(Instant.now());
        rolRepository.save(rol);

        log.info("Permisos asignados exitosamente al rol: {}", rolId);
    }

    @Override
    public void removePermisosFromRol(UUID rolId, List<UUID> permisoIds) {
        log.info("Removiendo {} permisos del rol {}", permisoIds.size(), rolId);

        Rol rol = rolRepository.findById(rolId)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));

        List<Permiso> permisos = permisoRepository.findAllById(permisoIds);
        rol.getPermisos().removeAll(permisos);
        
        rol.setUpdatedAt(Instant.now());
        rolRepository.save(rol);

        log.info("Permisos removidos exitosamente del rol: {}", rolId);
    }

    /**
     * Convierte una entidad Rol a RolWithPermisosDTO
     */
    private RolWithPermisosDTO toRolWithPermisosDTO(Rol rol) {
        Set<PermisoDTO> permisoDTOs = rol.getPermisos().stream()
            .map(permiso -> PermisoDTO.builder()
                .id(permiso.getId())
                .recurso(permiso.getRecurso())
                .accion(permiso.getAccion())
                .scope(permiso.getScope())
                .descripcion(permiso.getDescripcion())
                .createdAt(permiso.getCreatedAt())
                .build())
            .collect(Collectors.toSet());

        return RolWithPermisosDTO.builder()
            .id(rol.getId())
            .unidadNegocioId(rol.getUnidadNegocioId())
            .nombre(rol.getNombre())
            .descripcion(rol.getDescripcion())
            .departamento(rol.getDepartamento())
            .nivelJerarquico(rol.getNivelJerarquico())
            .permisos(permisoDTOs)
            .createdAt(rol.getCreatedAt())
            .updatedAt(rol.getUpdatedAt())
            .build();
    }
}
