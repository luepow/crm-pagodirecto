package com.pagodirecto.seguridad.application.service.impl;

import com.pagodirecto.seguridad.application.dto.CreatePermisoRequest;
import com.pagodirecto.seguridad.application.dto.PermisoDTO;
import com.pagodirecto.seguridad.application.dto.UpdatePermisoRequest;
import com.pagodirecto.seguridad.application.service.PermisoService;
import com.pagodirecto.seguridad.domain.Permiso;
import com.pagodirecto.seguridad.infrastructure.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación: PermisoServiceImpl
 *
 * Implementación del servicio de gestión CRUD de permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermisoDTO> getAllPermisos() {
        log.info("Obteniendo todos los permisos");
        return permisoRepository.findAll().stream()
            .map(this::toPermisoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoDTO> getPermisosByRecurso(String recurso) {
        log.info("Obteniendo permisos por recurso: {}", recurso);
        return permisoRepository.findByRecurso(recurso).stream()
            .map(this::toPermisoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoDTO> getPermisosByAccion(String accion) {
        log.info("Obteniendo permisos por acción: {}", accion);
        return permisoRepository.findByAccion(accion).stream()
            .map(this::toPermisoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermisoDTO getPermisoById(UUID id) {
        log.info("Obteniendo permiso por ID: {}", id);
        Permiso permiso = permisoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));
        return toPermisoDTO(permiso);
    }

    @Override
    public PermisoDTO createPermiso(CreatePermisoRequest request, UUID creatorId) {
        log.info("Creando nuevo permiso: {} - {}", request.getRecurso(), request.getAccion());

        // Verificar que no exista el permiso
        if (permisoRepository.existsByRecursoAndAccion(request.getRecurso(), request.getAccion())) {
            throw new RuntimeException("El permiso ya existe: " + request.getRecurso() + " - " + request.getAccion());
        }

        // Crear el permiso
        Permiso permiso = Permiso.builder()
            .recurso(request.getRecurso())
            .accion(request.getAccion())
            .scope(request.getScope())
            .descripcion(request.getDescripcion())
            .createdAt(Instant.now())
            .createdBy(creatorId)
            .updatedAt(Instant.now())
            .updatedBy(creatorId)
            .build();

        Permiso savedPermiso = permisoRepository.save(permiso);
        log.info("Permiso creado exitosamente: {}", savedPermiso.getId());

        return toPermisoDTO(savedPermiso);
    }

    @Override
    public PermisoDTO updatePermiso(UUID id, UpdatePermisoRequest request, UUID updaterId) {
        log.info("Actualizando permiso: {}", id);

        Permiso permiso = permisoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));

        // Actualizar datos (solo scope y descripción son modificables)
        permiso.setScope(request.getScope());
        permiso.setDescripcion(request.getDescripcion());
        permiso.setUpdatedAt(Instant.now());
        permiso.setUpdatedBy(updaterId);

        Permiso updatedPermiso = permisoRepository.save(permiso);
        log.info("Permiso actualizado exitosamente: {}", id);

        return toPermisoDTO(updatedPermiso);
    }

    @Override
    public void deletePermiso(UUID id) {
        log.info("Eliminando permiso (soft delete): {}", id);

        Permiso permiso = permisoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));

        // TODO: Verificar que no haya roles asignados a este permiso
        
        permisoRepository.delete(permiso);
        log.info("Permiso eliminado exitosamente: {}", id);
    }

    /**
     * Convierte una entidad Permiso a PermisoDTO
     */
    private PermisoDTO toPermisoDTO(Permiso permiso) {
        return PermisoDTO.builder()
            .id(permiso.getId())
            .recurso(permiso.getRecurso())
            .accion(permiso.getAccion())
            .scope(permiso.getScope())
            .descripcion(permiso.getDescripcion())
            .createdAt(permiso.getCreatedAt())
            .build();
    }
}
