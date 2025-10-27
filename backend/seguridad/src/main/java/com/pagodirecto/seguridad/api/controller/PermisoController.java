package com.pagodirecto.seguridad.api.controller;

import com.pagodirecto.seguridad.application.dto.CreatePermisoRequest;
import com.pagodirecto.seguridad.application.dto.PermisoDTO;
import com.pagodirecto.seguridad.application.dto.UpdatePermisoRequest;
import com.pagodirecto.seguridad.application.service.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST: PermisoController
 *
 * Endpoints para gestión CRUD de permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/permisos")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permisos", description = "API de gestión de permisos granulares")
public class PermisoController {

    private final PermisoService permisoService;

    /**
     * Obtiene todos los permisos
     */
    @GetMapping
    @Operation(summary = "Obtener todos los permisos")
    public ResponseEntity<List<PermisoDTO>> getAllPermisos() {
        log.info("GET /api/v1/permisos - Obtener todos los permisos");
        List<PermisoDTO> permisos = permisoService.getAllPermisos();
        return ResponseEntity.ok(permisos);
    }

    /**
     * Obtiene permisos por recurso
     */
    @GetMapping("/recurso/{recurso}")
    @Operation(summary = "Obtener permisos por recurso")
    public ResponseEntity<List<PermisoDTO>> getPermisosByRecurso(
        @PathVariable String recurso
    ) {
        log.info("GET /api/v1/permisos/recurso/{}", recurso);
        List<PermisoDTO> permisos = permisoService.getPermisosByRecurso(recurso);
        return ResponseEntity.ok(permisos);
    }

    /**
     * Obtiene permisos por acción
     */
    @GetMapping("/accion/{accion}")
    @Operation(summary = "Obtener permisos por acción")
    public ResponseEntity<List<PermisoDTO>> getPermisosByAccion(
        @PathVariable String accion
    ) {
        log.info("GET /api/v1/permisos/accion/{}", accion);
        List<PermisoDTO> permisos = permisoService.getPermisosByAccion(accion);
        return ResponseEntity.ok(permisos);
    }

    /**
     * Obtiene un permiso por su ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener permiso por ID")
    public ResponseEntity<PermisoDTO> getPermisoById(@PathVariable UUID id) {
        log.info("GET /api/v1/permisos/{}", id);
        PermisoDTO permiso = permisoService.getPermisoById(id);
        return ResponseEntity.ok(permiso);
    }

    /**
     * Crea un nuevo permiso
     */
    @PostMapping
    @Operation(summary = "Crear nuevo permiso")
    public ResponseEntity<PermisoDTO> createPermiso(
        @Valid @RequestBody CreatePermisoRequest request
    ) {
        log.info("POST /api/v1/permisos - Crear permiso: {} - {}", request.getRecurso(), request.getAccion());
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        PermisoDTO permiso = permisoService.createPermiso(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(permiso);
    }

    /**
     * Actualiza un permiso existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar permiso")
    public ResponseEntity<PermisoDTO> updatePermiso(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePermisoRequest request
    ) {
        log.info("PUT /api/v1/permisos/{}", id);
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID updaterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        PermisoDTO permiso = permisoService.updatePermiso(id, request, updaterId);
        return ResponseEntity.ok(permiso);
    }

    /**
     * Elimina un permiso (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar permiso (soft delete)")
    public ResponseEntity<Void> deletePermiso(@PathVariable UUID id) {
        log.info("DELETE /api/v1/permisos/{}", id);
        permisoService.deletePermiso(id);
        return ResponseEntity.noContent().build();
    }
}
