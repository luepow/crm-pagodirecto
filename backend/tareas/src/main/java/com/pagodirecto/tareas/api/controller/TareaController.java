package com.pagodirecto.tareas.api.controller;

import com.pagodirecto.tareas.application.dto.TareaDTO;
import com.pagodirecto.tareas.application.service.TareaService;
import com.pagodirecto.tareas.domain.StatusTarea;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller: Tareas
 *
 * Endpoints para gestión de tareas y actividades.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/tareas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tareas", description = "API de gestión de tareas y actividades")
public class TareaController {

    private final TareaService tareaService;

    @PostMapping
    @Operation(summary = "Crear nueva tarea")
    public ResponseEntity<TareaDTO> crear(
            @Valid @RequestBody TareaDTO tareaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para crear tarea: {} por usuario: {}",
                tareaDTO.getTitulo(), userDetails.getUsername());

        TareaDTO tareaCreada = tareaService.crear(tareaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaCreada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarea existente")
    public ResponseEntity<TareaDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody TareaDTO tareaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para actualizar tarea {} por usuario: {}",
                id, userDetails.getUsername());

        TareaDTO tareaActualizada = tareaService.actualizar(id, tareaDTO);
        return ResponseEntity.ok(tareaActualizada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarea por ID")
    public ResponseEntity<TareaDTO> obtenerPorId(@PathVariable UUID id) {
        log.debug("Solicitud para obtener tarea con ID: {}", id);
        TareaDTO tarea = tareaService.buscarPorId(id);
        return ResponseEntity.ok(tarea);
    }

    @GetMapping
    @Operation(summary = "Listar todas las tareas con paginación")
    public ResponseEntity<Page<TareaDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        log.debug("Solicitud para listar tareas - página: {}, tamaño: {}", page, size);

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.listar(pageable);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/asignado/{asignadoA}")
    @Operation(summary = "Listar tareas asignadas a un usuario")
    public ResponseEntity<Page<TareaDTO>> listarPorAsignado(
            @PathVariable UUID asignadoA,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaVencimiento,asc") String[] sort) {
        log.debug("Solicitud para listar tareas asignadas a: {}", asignadoA);

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.listarPorAsignado(asignadoA, pageable);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar tareas por status")
    public ResponseEntity<Page<TareaDTO>> listarPorStatus(
            @PathVariable StatusTarea status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaVencimiento,asc") String[] sort) {
        log.debug("Solicitud para listar tareas con status: {}", status);

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.listarPorStatus(status, pageable);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/relacionado/{tipo}/{id}")
    @Operation(summary = "Listar tareas relacionadas a una entidad")
    public ResponseEntity<Page<TareaDTO>> listarPorRelacionado(
            @PathVariable String tipo,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        log.debug("Solicitud para listar tareas relacionadas a {} con ID: {}", tipo, id);

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.listarPorRelacionado(tipo, id, pageable);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/vencidas")
    @Operation(summary = "Listar tareas vencidas")
    public ResponseEntity<Page<TareaDTO>> listarVencidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaVencimiento,asc") String[] sort) {
        log.debug("Solicitud para listar tareas vencidas");

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.listarVencidas(pageable);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/por-vencer")
    @Operation(summary = "Listar tareas por vencer en los próximos N días")
    public ResponseEntity<Page<TareaDTO>> listarPorVencer(
            @RequestParam(defaultValue = "7") int dias,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaVencimiento,asc") String[] sort) {
        log.debug("Solicitud para listar tareas por vencer en {} días", dias);

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.listarPorVencer(dias, pageable);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar tareas por texto")
    public ResponseEntity<Page<TareaDTO>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        log.debug("Solicitud de búsqueda de tareas con query: {}", q);

        Pageable pageable = crearPageable(page, size, sort);
        Page<TareaDTO> tareas = tareaService.buscar(q, pageable);
        return ResponseEntity.ok(tareas);
    }

    @PutMapping("/{id}/completar")
    @Operation(summary = "Marcar tarea como completada")
    public ResponseEntity<TareaDTO> completar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para completar tarea {} por usuario: {}",
                id, userDetails.getUsername());

        TareaDTO tareaCompletada = tareaService.completar(id);
        return ResponseEntity.ok(tareaCompletada);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar tarea")
    public ResponseEntity<TareaDTO> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para cancelar tarea {} por usuario: {}",
                id, userDetails.getUsername());

        TareaDTO tareaCancelada = tareaService.cancelar(id);
        return ResponseEntity.ok(tareaCancelada);
    }

    @PutMapping("/{id}/reasignar")
    @Operation(summary = "Reasignar tarea a otro usuario")
    public ResponseEntity<TareaDTO> reasignar(
            @PathVariable UUID id,
            @RequestBody Map<String, UUID> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID nuevoAsignadoId = body.get("asignadoA");
        log.info("Solicitud para reasignar tarea {} a usuario {} por: {}",
                id, nuevoAsignadoId, userDetails.getUsername());

        TareaDTO tareaReasignada = tareaService.reasignar(id, nuevoAsignadoId);
        return ResponseEntity.ok(tareaReasignada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarea (soft delete)")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para eliminar tarea {} por usuario: {}",
                id, userDetails.getUsername());

        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/asignado/{usuarioId}/status/{status}")
    @Operation(summary = "Contar tareas por usuario y status")
    public ResponseEntity<Long> contarPorAsignadoYStatus(
            @PathVariable UUID usuarioId,
            @PathVariable StatusTarea status) {
        log.debug("Solicitud para contar tareas de usuario {} con status {}", usuarioId, status);

        long count = tareaService.contarPorAsignadoYStatus(usuarioId, status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/inactivas")
    @Operation(summary = "Encontrar tareas sin actualizar en X días")
    public ResponseEntity<List<TareaDTO>> encontrarInactivas(
            @RequestParam(defaultValue = "30") int dias) {
        log.debug("Solicitud para encontrar tareas inactivas por más de {} días", dias);

        List<TareaDTO> tareasInactivas = tareaService.encontrarTareasInactivas(dias);
        return ResponseEntity.ok(tareasInactivas);
    }

    /**
     * Helper method para crear Pageable con sort dinámico
     */
    private Pageable crearPageable(int page, int size, String[] sort) {
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
