package com.pagodirecto.tareas.application.service.impl;

import com.pagodirecto.tareas.application.dto.TareaDTO;
import com.pagodirecto.tareas.application.mapper.TareaMapper;
import com.pagodirecto.tareas.application.service.TareaService;
import com.pagodirecto.tareas.domain.StatusTarea;
import com.pagodirecto.tareas.domain.Tarea;
import com.pagodirecto.tareas.infrastructure.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service Implementation: TareaServiceImpl
 *
 * Implementación del servicio de gestión de tareas.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;

    @Override
    public TareaDTO crear(TareaDTO tareaDTO) {
        log.info("Creando nueva tarea: {}", tareaDTO.getTitulo());

        Tarea tarea = tareaMapper.toEntity(tareaDTO);
        tarea.setCreatedAt(Instant.now());
        tarea.setUpdatedAt(Instant.now());

        Tarea tareaGuardada = tareaRepository.save(tarea);
        log.info("Tarea creada exitosamente con ID: {}", tareaGuardada.getId());

        return tareaMapper.toDTO(tareaGuardada);
    }

    @Override
    public TareaDTO actualizar(UUID id, TareaDTO tareaDTO) {
        log.info("Actualizando tarea con ID: {}", id);

        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));

        tareaMapper.updateEntityFromDTO(tareaDTO, tarea);
        tarea.setUpdatedAt(Instant.now());

        Tarea tareaActualizada = tareaRepository.save(tarea);
        log.info("Tarea actualizada exitosamente: {}", id);

        return tareaMapper.toDTO(tareaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public TareaDTO buscarPorId(UUID id) {
        log.debug("Buscando tarea con ID: {}", id);

        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));

        return tareaMapper.toDTO(tarea);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> listar(Pageable pageable) {
        log.debug("Listando todas las tareas con paginación");
        return tareaRepository.findAll(pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> listarPorAsignado(UUID asignadoA, Pageable pageable) {
        log.debug("Listando tareas asignadas a: {}", asignadoA);
        return tareaRepository.findByAsignadoA(asignadoA, pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> listarPorStatus(StatusTarea status, Pageable pageable) {
        log.debug("Listando tareas con status: {}", status);
        return tareaRepository.findByStatus(status, pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> listarPorRelacionado(String tipo, UUID id, Pageable pageable) {
        log.debug("Listando tareas relacionadas a {} con ID: {}", tipo, id);
        return tareaRepository.findByRelacionadoTipoAndRelacionadoId(tipo, id, pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> listarVencidas(Pageable pageable) {
        log.debug("Listando tareas vencidas");
        return tareaRepository.findTareasVencidas(LocalDate.now(), pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> listarPorVencer(int dias, Pageable pageable) {
        log.debug("Listando tareas por vencer en {} días", dias);
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        return tareaRepository.findTareasPorVencer(fechaInicio, fechaFin, pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaDTO> buscar(String query, Pageable pageable) {
        log.debug("Buscando tareas con query: {}", query);
        return tareaRepository.searchByText(query, pageable)
                .map(tareaMapper::toDTO);
    }

    @Override
    public TareaDTO completar(UUID id) {
        log.info("Completando tarea con ID: {}", id);

        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));

        tarea.completar();
        Tarea tareaCompletada = tareaRepository.save(tarea);

        log.info("Tarea completada exitosamente: {}", id);
        return tareaMapper.toDTO(tareaCompletada);
    }

    @Override
    public TareaDTO cancelar(UUID id) {
        log.info("Cancelando tarea con ID: {}", id);

        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));

        tarea.cancelar();
        Tarea tareaCancelada = tareaRepository.save(tarea);

        log.info("Tarea cancelada exitosamente: {}", id);
        return tareaMapper.toDTO(tareaCancelada);
    }

    @Override
    public TareaDTO reasignar(UUID id, UUID nuevoAsignadoId) {
        log.info("Reasignando tarea {} a usuario: {}", id, nuevoAsignadoId);

        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada con ID: " + id));

        tarea.asignar(nuevoAsignadoId);
        Tarea tareaReasignada = tareaRepository.save(tarea);

        log.info("Tarea reasignada exitosamente: {}", id);
        return tareaMapper.toDTO(tareaReasignada);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando tarea con ID: {}", id);

        if (!tareaRepository.existsById(id)) {
            throw new IllegalArgumentException("Tarea no encontrada con ID: " + id);
        }

        tareaRepository.deleteById(id);
        log.info("Tarea eliminada exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorAsignadoYStatus(UUID usuarioId, StatusTarea status) {
        log.debug("Contando tareas para usuario {} con status {}", usuarioId, status);
        return tareaRepository.countByAsignadoAAndStatus(usuarioId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaDTO> encontrarTareasInactivas(int diasInactividad) {
        log.debug("Buscando tareas inactivas por más de {} días", diasInactividad);
        Instant fechaLimite = Instant.now().minus(diasInactividad, ChronoUnit.DAYS);

        return tareaRepository.findTareasInactivas(fechaLimite)
                .stream()
                .map(tareaMapper::toDTO)
                .toList();
    }
}
