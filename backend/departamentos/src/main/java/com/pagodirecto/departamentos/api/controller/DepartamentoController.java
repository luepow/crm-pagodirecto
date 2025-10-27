package com.pagodirecto.departamentos.api.controller;

import com.pagodirecto.departamentos.application.dto.CreateDepartamentoRequest;
import com.pagodirecto.departamentos.application.dto.DepartamentoDTO;
import com.pagodirecto.departamentos.application.dto.UpdateDepartamentoRequest;
import com.pagodirecto.departamentos.application.service.DepartamentoService;
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
 * Controlador REST: DepartamentoController
 *
 * Endpoints para gestión CRUD de departamentos
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/departamentos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Departamentos", description = "API de gestión de departamentos organizacionales")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    /**
     * Obtiene todos los departamentos
     */
    @GetMapping
    @Operation(summary = "Obtener todos los departamentos")
    public ResponseEntity<List<DepartamentoDTO>> getAllDepartamentos() {
        log.info("GET /api/v1/departamentos - Obtener todos los departamentos");
        List<DepartamentoDTO> departamentos = departamentoService.getAllDepartamentos();
        return ResponseEntity.ok(departamentos);
    }

    /**
     * Obtiene departamentos por unidad de negocio
     */
    @GetMapping("/unidad-negocio/{unidadNegocioId}")
    @Operation(summary = "Obtener departamentos por unidad de negocio")
    public ResponseEntity<List<DepartamentoDTO>> getDepartamentosByUnidadNegocio(
        @PathVariable UUID unidadNegocioId
    ) {
        log.info("GET /api/v1/departamentos/unidad-negocio/{}", unidadNegocioId);
        List<DepartamentoDTO> departamentos = departamentoService.getDepartamentosByUnidadNegocio(unidadNegocioId);
        return ResponseEntity.ok(departamentos);
    }

    /**
     * Obtiene departamentos activos por unidad de negocio
     */
    @GetMapping("/unidad-negocio/{unidadNegocioId}/activos")
    @Operation(summary = "Obtener departamentos activos por unidad de negocio")
    public ResponseEntity<List<DepartamentoDTO>> getDepartamentosActivosByUnidadNegocio(
        @PathVariable UUID unidadNegocioId
    ) {
        log.info("GET /api/v1/departamentos/unidad-negocio/{}/activos", unidadNegocioId);
        List<DepartamentoDTO> departamentos = departamentoService.getDepartamentosActivosByUnidadNegocio(unidadNegocioId);
        return ResponseEntity.ok(departamentos);
    }

    /**
     * Obtiene departamentos raíz (sin padre)
     */
    @GetMapping("/raiz")
    @Operation(summary = "Obtener departamentos raíz")
    public ResponseEntity<List<DepartamentoDTO>> getDepartamentosRaiz() {
        log.info("GET /api/v1/departamentos/raiz");
        List<DepartamentoDTO> departamentos = departamentoService.getDepartamentosRaiz();
        return ResponseEntity.ok(departamentos);
    }

    /**
     * Obtiene sub-departamentos de un departamento
     */
    @GetMapping("/{parentId}/sub-departamentos")
    @Operation(summary = "Obtener sub-departamentos")
    public ResponseEntity<List<DepartamentoDTO>> getSubDepartamentos(@PathVariable UUID parentId) {
        log.info("GET /api/v1/departamentos/{}/sub-departamentos", parentId);
        List<DepartamentoDTO> departamentos = departamentoService.getSubDepartamentos(parentId);
        return ResponseEntity.ok(departamentos);
    }

    /**
     * Obtiene un departamento por su ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener departamento por ID")
    public ResponseEntity<DepartamentoDTO> getDepartamentoById(@PathVariable UUID id) {
        log.info("GET /api/v1/departamentos/{}", id);
        DepartamentoDTO departamento = departamentoService.getDepartamentoById(id);
        return ResponseEntity.ok(departamento);
    }

    /**
     * Obtiene un departamento por su código
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Obtener departamento por código")
    public ResponseEntity<DepartamentoDTO> getDepartamentoByCodigo(@PathVariable String codigo) {
        log.info("GET /api/v1/departamentos/codigo/{}", codigo);
        DepartamentoDTO departamento = departamentoService.getDepartamentoByCodigo(codigo);
        return ResponseEntity.ok(departamento);
    }

    /**
     * Crea un nuevo departamento
     */
    @PostMapping
    @Operation(summary = "Crear nuevo departamento")
    public ResponseEntity<DepartamentoDTO> createDepartamento(
        @Valid @RequestBody CreateDepartamentoRequest request
    ) {
        log.info("POST /api/v1/departamentos - Crear departamento: {}", request.getCodigo());
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        DepartamentoDTO departamento = departamentoService.createDepartamento(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(departamento);
    }

    /**
     * Actualiza un departamento existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar departamento")
    public ResponseEntity<DepartamentoDTO> updateDepartamento(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateDepartamentoRequest request
    ) {
        log.info("PUT /api/v1/departamentos/{}", id);
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID updaterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        DepartamentoDTO departamento = departamentoService.updateDepartamento(id, request, updaterId);
        return ResponseEntity.ok(departamento);
    }

    /**
     * Elimina un departamento (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar departamento (soft delete)")
    public ResponseEntity<Void> deleteDepartamento(@PathVariable UUID id) {
        log.info("DELETE /api/v1/departamentos/{}", id);
        departamentoService.deleteDepartamento(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activa/desactiva un departamento
     */
    @PostMapping("/{id}/toggle-activo")
    @Operation(summary = "Activar/desactivar departamento")
    public ResponseEntity<Void> toggleActivoDepartamento(@PathVariable UUID id) {
        log.info("POST /api/v1/departamentos/{}/toggle-activo", id);
        departamentoService.toggleActivoDepartamento(id);
        return ResponseEntity.ok().build();
    }
}
