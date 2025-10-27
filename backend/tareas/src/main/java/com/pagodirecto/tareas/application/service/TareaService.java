package com.pagodirecto.tareas.application.service;

import com.pagodirecto.tareas.application.dto.TareaDTO;
import com.pagodirecto.tareas.domain.StatusTarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service: TareaService
 *
 * Interfaz de servicio para gestión de tareas.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface TareaService {

    /**
     * Crea una nueva tarea
     */
    TareaDTO crear(TareaDTO tareaDTO);

    /**
     * Actualiza una tarea existente
     */
    TareaDTO actualizar(UUID id, TareaDTO tareaDTO);

    /**
     * Busca una tarea por ID
     */
    TareaDTO buscarPorId(UUID id);

    /**
     * Lista todas las tareas con paginación
     */
    Page<TareaDTO> listar(Pageable pageable);

    /**
     * Lista tareas asignadas a un usuario
     */
    Page<TareaDTO> listarPorAsignado(UUID asignadoA, Pageable pageable);

    /**
     * Lista tareas por status
     */
    Page<TareaDTO> listarPorStatus(StatusTarea status, Pageable pageable);

    /**
     * Lista tareas relacionadas a una entidad
     */
    Page<TareaDTO> listarPorRelacionado(String tipo, UUID id, Pageable pageable);

    /**
     * Lista tareas vencidas
     */
    Page<TareaDTO> listarVencidas(Pageable pageable);

    /**
     * Lista tareas por vencer en los próximos N días
     */
    Page<TareaDTO> listarPorVencer(int dias, Pageable pageable);

    /**
     * Búsqueda de texto en tareas
     */
    Page<TareaDTO> buscar(String query, Pageable pageable);

    /**
     * Completa una tarea
     */
    TareaDTO completar(UUID id);

    /**
     * Cancela una tarea
     */
    TareaDTO cancelar(UUID id);

    /**
     * Reasigna una tarea a otro usuario
     */
    TareaDTO reasignar(UUID id, UUID nuevoAsignadoId);

    /**
     * Elimina una tarea (soft delete)
     */
    void eliminar(UUID id);

    /**
     * Cuenta tareas por status para un usuario
     */
    long contarPorAsignadoYStatus(UUID usuarioId, StatusTarea status);

    /**
     * Encuentra tareas sin actualizar en X días
     */
    List<TareaDTO> encontrarTareasInactivas(int diasInactividad);
}
