package com.pagodirecto.departamentos.application.service.impl;

import com.pagodirecto.departamentos.application.dto.CreateDepartamentoRequest;
import com.pagodirecto.departamentos.application.dto.DepartamentoDTO;
import com.pagodirecto.departamentos.application.dto.UpdateDepartamentoRequest;
import com.pagodirecto.departamentos.application.service.DepartamentoService;
import com.pagodirecto.departamentos.domain.Departamento;
import com.pagodirecto.departamentos.infrastructure.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación: DepartamentoServiceImpl
 *
 * Implementación del servicio de gestión CRUD de departamentos
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DepartamentoServiceImpl implements DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> getAllDepartamentos() {
        log.info("Obteniendo todos los departamentos");
        return departamentoRepository.findAll().stream()
            .map(this::toDepartamentoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> getDepartamentosByUnidadNegocio(UUID unidadNegocioId) {
        log.info("Obteniendo departamentos por unidad de negocio: {}", unidadNegocioId);
        return departamentoRepository.findByUnidadNegocioId(unidadNegocioId).stream()
            .map(this::toDepartamentoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> getDepartamentosActivosByUnidadNegocio(UUID unidadNegocioId) {
        log.info("Obteniendo departamentos activos por unidad de negocio: {}", unidadNegocioId);
        return departamentoRepository.findByUnidadNegocioIdAndActivoTrue(unidadNegocioId).stream()
            .map(this::toDepartamentoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> getDepartamentosRaiz() {
        log.info("Obteniendo departamentos raíz");
        return departamentoRepository.findByParentIdIsNull().stream()
            .map(this::toDepartamentoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> getSubDepartamentos(UUID parentId) {
        log.info("Obteniendo sub-departamentos de: {}", parentId);
        return departamentoRepository.findByParentId(parentId).stream()
            .map(this::toDepartamentoDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartamentoDTO getDepartamentoById(UUID id) {
        log.info("Obteniendo departamento por ID: {}", id);
        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + id));
        return toDepartamentoDTO(departamento);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartamentoDTO getDepartamentoByCodigo(String codigo) {
        log.info("Obteniendo departamento por código: {}", codigo);
        Departamento departamento = departamentoRepository.findByCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Departamento no encontrado con código: " + codigo));
        return toDepartamentoDTO(departamento);
    }

    @Override
    public DepartamentoDTO createDepartamento(CreateDepartamentoRequest request, UUID creatorId) {
        log.info("Creando nuevo departamento: {}", request.getCodigo());

        // Verificar que no exista el código
        if (departamentoRepository.existsByCodigo(request.getCodigo())) {
            throw new RuntimeException("El código ya existe: " + request.getCodigo());
        }

        // Calcular nivel y path
        Integer nivel = 0;
        String parentPath = null;
        if (request.getParentId() != null) {
            Departamento parent = departamentoRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Departamento padre no encontrado"));
            nivel = parent.getNivel() + 1;
            parentPath = parent.getPath();
            
            if (nivel > 5) {
                throw new RuntimeException("No se puede crear departamento: nivel máximo alcanzado (5)");
            }
        }

        // Crear el departamento
        Departamento departamento = Departamento.builder()
            .unidadNegocioId(request.getUnidadNegocioId())
            .codigo(request.getCodigo())
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .parentId(request.getParentId())
            .nivel(nivel)
            .jefeId(request.getJefeId())
            .emailDepartamento(request.getEmailDepartamento())
            .telefonoDepartamento(request.getTelefonoDepartamento())
            .ubicacion(request.getUbicacion())
            .presupuestoAnual(request.getPresupuestoAnual())
            .numeroEmpleados(request.getNumeroEmpleados() != null ? request.getNumeroEmpleados() : 0)
            .activo(request.getActivo() != null ? request.getActivo() : true)
            .createdAt(Instant.now())
            .createdBy(creatorId)
            .updatedAt(Instant.now())
            .updatedBy(creatorId)
            .build();

        // Construir path
        departamento.buildPath(parentPath);

        Departamento saved = departamentoRepository.save(departamento);
        log.info("Departamento creado exitosamente: {}", saved.getId());

        return toDepartamentoDTO(saved);
    }

    @Override
    public DepartamentoDTO updateDepartamento(UUID id, UpdateDepartamentoRequest request, UUID updaterId) {
        log.info("Actualizando departamento: {}", id);

        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + id));

        // Actualizar datos
        departamento.setNombre(request.getNombre());
        departamento.setDescripcion(request.getDescripcion());
        departamento.setJefeId(request.getJefeId());
        departamento.setEmailDepartamento(request.getEmailDepartamento());
        departamento.setTelefonoDepartamento(request.getTelefonoDepartamento());
        departamento.setUbicacion(request.getUbicacion());
        departamento.setPresupuestoAnual(request.getPresupuestoAnual());
        departamento.setNumeroEmpleados(request.getNumeroEmpleados());
        
        if (request.getActivo() != null) {
            departamento.setActivo(request.getActivo());
        }

        // Si cambió el padre, recalcular nivel y path
        if (request.getParentId() != null && !request.getParentId().equals(departamento.getParentId())) {
            Departamento parent = departamentoRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Departamento padre no encontrado"));
            
            Integer newNivel = parent.getNivel() + 1;
            if (newNivel > 5) {
                throw new RuntimeException("No se puede mover: nivel máximo alcanzado (5)");
            }
            
            departamento.setParentId(request.getParentId());
            departamento.setNivel(newNivel);
            departamento.buildPath(parent.getPath());
        } else if (request.getParentId() == null && departamento.getParentId() != null) {
            // Moviendo a raíz
            departamento.setParentId(null);
            departamento.setNivel(0);
            departamento.buildPath(null);
        } else {
            // Solo actualizar path si cambió el nombre
            String parentPath = null;
            if (departamento.getParentId() != null) {
                Departamento parent = departamentoRepository.findById(departamento.getParentId())
                    .orElseThrow(() -> new RuntimeException("Departamento padre no encontrado"));
                parentPath = parent.getPath();
            }
            departamento.buildPath(parentPath);
        }

        departamento.setUpdatedAt(Instant.now());
        departamento.setUpdatedBy(updaterId);

        Departamento updated = departamentoRepository.save(departamento);
        log.info("Departamento actualizado exitosamente: {}", id);

        return toDepartamentoDTO(updated);
    }

    @Override
    public void deleteDepartamento(UUID id) {
        log.info("Eliminando departamento (soft delete): {}", id);

        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + id));

        // Verificar que no tenga sub-departamentos
        Long subDepartamentos = departamentoRepository.countByParentId(id);
        if (subDepartamentos > 0) {
            throw new RuntimeException("No se puede eliminar: el departamento tiene " + subDepartamentos + " sub-departamentos");
        }

        departamentoRepository.delete(departamento);
        log.info("Departamento eliminado exitosamente: {}", id);
    }

    @Override
    public void toggleActivoDepartamento(UUID id) {
        log.info("Cambiando estado activo de departamento: {}", id);

        Departamento departamento = departamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + id));

        departamento.setActivo(!departamento.getActivo());
        departamento.setUpdatedAt(Instant.now());
        departamentoRepository.save(departamento);

        log.info("Estado activo cambiado exitosamente: {} -> {}", id, departamento.getActivo());
    }

    /**
     * Convierte una entidad Departamento a DepartamentoDTO
     */
    private DepartamentoDTO toDepartamentoDTO(Departamento departamento) {
        DepartamentoDTO dto = DepartamentoDTO.builder()
            .id(departamento.getId())
            .unidadNegocioId(departamento.getUnidadNegocioId())
            .codigo(departamento.getCodigo())
            .nombre(departamento.getNombre())
            .descripcion(departamento.getDescripcion())
            .parentId(departamento.getParentId())
            .nivel(departamento.getNivel())
            .path(departamento.getPath())
            .jefeId(departamento.getJefeId())
            .emailDepartamento(departamento.getEmailDepartamento())
            .telefonoDepartamento(departamento.getTelefonoDepartamento())
            .ubicacion(departamento.getUbicacion())
            .presupuestoAnual(departamento.getPresupuestoAnual())
            .numeroEmpleados(departamento.getNumeroEmpleados())
            .activo(departamento.getActivo())
            .createdAt(departamento.getCreatedAt())
            .updatedAt(departamento.getUpdatedAt())
            .build();

        // Obtener nombre del padre si existe
        if (departamento.getParentId() != null) {
            departamentoRepository.findById(departamento.getParentId())
                .ifPresent(parent -> dto.setParentNombre(parent.getNombre()));
        }

        // TODO: Obtener nombre del jefe desde UsuarioRepository
        // Temporalmente dejamos null

        return dto;
    }
}
