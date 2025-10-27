package com.pagodirecto.seguridad.api.controller;

import com.pagodirecto.seguridad.application.dto.CreateRolRequest;
import com.pagodirecto.seguridad.application.dto.RolWithPermisosDTO;
import com.pagodirecto.seguridad.application.dto.UpdateRolRequest;
import com.pagodirecto.seguridad.application.service.RolService;
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
 * Controlador REST: RolController
 *
 * Endpoints para gestión CRUD de roles y asignación de permisos
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/roles")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles", description = "API de gestión de roles y permisos (RBAC)")
public class RolController {

    private final RolService rolService;

    /**
     * Obtiene todos los roles
     */
    @GetMapping
    @Operation(summary = "Obtener todos los roles")
    public ResponseEntity<List<RolWithPermisosDTO>> getAllRoles() {
        log.info("GET /api/v1/roles - Obtener todos los roles");
        List<RolWithPermisosDTO> roles = rolService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtiene roles por unidad de negocio
     */
    @GetMapping("/unidad-negocio/{unidadNegocioId}")
    @Operation(summary = "Obtener roles por unidad de negocio")
    public ResponseEntity<List<RolWithPermisosDTO>> getRolesByUnidadNegocio(
        @PathVariable UUID unidadNegocioId
    ) {
        log.info("GET /api/v1/roles/unidad-negocio/{}", unidadNegocioId);
        List<RolWithPermisosDTO> roles = rolService.getRolesByUnidadNegocio(unidadNegocioId);
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtiene roles por departamento
     */
    @GetMapping("/departamento/{departamento}")
    @Operation(summary = "Obtener roles por departamento")
    public ResponseEntity<List<RolWithPermisosDTO>> getRolesByDepartamento(
        @PathVariable String departamento
    ) {
        log.info("GET /api/v1/roles/departamento/{}", departamento);
        List<RolWithPermisosDTO> roles = rolService.getRolesByDepartamento(departamento);
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtiene un rol por su ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<RolWithPermisosDTO> getRolById(@PathVariable UUID id) {
        log.info("GET /api/v1/roles/{}", id);
        RolWithPermisosDTO rol = rolService.getRolById(id);
        return ResponseEntity.ok(rol);
    }

    /**
     * Crea un nuevo rol
     */
    @PostMapping
    @Operation(summary = "Crear nuevo rol")
    public ResponseEntity<RolWithPermisosDTO> createRol(
        @Valid @RequestBody CreateRolRequest request
    ) {
        log.info("POST /api/v1/roles - Crear rol: {}", request.getNombre());
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        RolWithPermisosDTO rol = rolService.createRol(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(rol);
    }

    /**
     * Actualiza un rol existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
    public ResponseEntity<RolWithPermisosDTO> updateRol(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateRolRequest request
    ) {
        log.info("PUT /api/v1/roles/{}", id);
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID updaterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        RolWithPermisosDTO rol = rolService.updateRol(id, request, updaterId);
        return ResponseEntity.ok(rol);
    }

    /**
     * Elimina un rol (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol (soft delete)")
    public ResponseEntity<Void> deleteRol(@PathVariable UUID id) {
        log.info("DELETE /api/v1/roles/{}", id);
        rolService.deleteRol(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Asigna permisos a un rol
     */
    @PostMapping("/{id}/permisos")
    @Operation(summary = "Asignar permisos a un rol")
    public ResponseEntity<Void> assignPermisos(
        @PathVariable UUID id,
        @RequestBody List<UUID> permisoIds
    ) {
        log.info("POST /api/v1/roles/{}/permisos - Asignar {} permisos", id, permisoIds.size());
        rolService.assignPermisosToRol(id, permisoIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Remueve permisos de un rol
     */
    @DeleteMapping("/{id}/permisos")
    @Operation(summary = "Remover permisos de un rol")
    public ResponseEntity<Void> removePermisos(
        @PathVariable UUID id,
        @RequestBody List<UUID> permisoIds
    ) {
        log.info("DELETE /api/v1/roles/{}/permisos - Remover {} permisos", id, permisoIds.size());
        rolService.removePermisosFromRol(id, permisoIds);
        return ResponseEntity.ok().build();
    }
}
