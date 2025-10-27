package com.pagodirecto.tareas.infrastructure.repository;

import com.pagodirecto.tareas.domain.StatusTarea;
import com.pagodirecto.tareas.domain.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository: Tarea
 *
 * Repositorio JPA para operaciones de persistencia de Tareas.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, UUID> {

    /**
     * Busca tareas asignadas a un usuario específico
     */
    Page<Tarea> findByAsignadoA(UUID asignadoA, Pageable pageable);

    /**
     * Busca tareas por status
     */
    Page<Tarea> findByStatus(StatusTarea status, Pageable pageable);

    /**
     * Busca tareas relacionadas a una entidad específica
     */
    Page<Tarea> findByRelacionadoTipoAndRelacionadoId(
        String relacionadoTipo,
        UUID relacionadoId,
        Pageable pageable
    );

    /**
     * Busca tareas vencidas (fecha vencimiento pasada y status != COMPLETADA)
     */
    @Query("SELECT t FROM Tarea t WHERE t.fechaVencimiento < :fecha " +
           "AND t.status NOT IN ('COMPLETADA', 'CANCELADA')")
    Page<Tarea> findTareasVencidas(@Param("fecha") LocalDate fecha, Pageable pageable);

    /**
     * Busca tareas por vencer en los próximos N días
     */
    @Query("SELECT t FROM Tarea t WHERE t.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin " +
           "AND t.status NOT IN ('COMPLETADA', 'CANCELADA')")
    Page<Tarea> findTareasPorVencer(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        Pageable pageable
    );

    /**
     * Busca tareas asignadas a un usuario con status específico
     */
    Page<Tarea> findByAsignadoAAndStatus(
        UUID asignadoA,
        StatusTarea status,
        Pageable pageable
    );

    /**
     * Búsqueda de texto completo en título y descripción
     */
    @Query("SELECT t FROM Tarea t WHERE " +
           "LOWER(t.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Tarea> searchByText(@Param("query") String query, Pageable pageable);

    /**
     * Cuenta tareas por status para un usuario
     */
    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.asignadoA = :usuarioId AND t.status = :status")
    long countByAsignadoAAndStatus(@Param("usuarioId") UUID usuarioId, @Param("status") StatusTarea status);

    /**
     * Encuentra tareas sin actualizar en X días
     */
    @Query("SELECT t FROM Tarea t WHERE t.updatedAt < :fechaLimite " +
           "AND t.status NOT IN ('COMPLETADA', 'CANCELADA')")
    List<Tarea> findTareasInactivas(@Param("fechaLimite") java.time.Instant fechaLimite);
}
