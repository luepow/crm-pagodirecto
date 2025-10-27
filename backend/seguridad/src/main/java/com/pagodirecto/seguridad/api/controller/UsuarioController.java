package com.pagodirecto.seguridad.api.controller;

import com.pagodirecto.seguridad.application.dto.CreateUsuarioRequest;
import com.pagodirecto.seguridad.application.dto.UpdateUsuarioRequest;
import com.pagodirecto.seguridad.application.dto.UsuarioDTO;
import com.pagodirecto.seguridad.application.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST: UsuarioController
 *
 * Endpoints para gestión CRUD de usuarios
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/usuarios")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "API de gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene todos los usuarios
     */
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        log.info("GET /api/v1/usuarios - Obtener todos los usuarios");
        List<UsuarioDTO> usuarios = usuarioService.getAllUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene usuarios por unidad de negocio
     */
    @GetMapping("/unidad-negocio/{unidadNegocioId}")
    @Operation(summary = "Obtener usuarios por unidad de negocio")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByUnidadNegocio(
        @PathVariable UUID unidadNegocioId
    ) {
        log.info("GET /api/v1/usuarios/unidad-negocio/{} - Obtener usuarios", unidadNegocioId);
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByUnidadNegocio(unidadNegocioId);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene un usuario por su ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable UUID id) {
        log.info("GET /api/v1/usuarios/{} - Obtener usuario", id);
        UsuarioDTO usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Crea un nuevo usuario
     */
    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<UsuarioDTO> createUsuario(
        @Valid @RequestBody CreateUsuarioRequest request
    ) {
        log.info("POST /api/v1/usuarios - Crear usuario: {}", request.getUsername());
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UsuarioDTO usuario = usuarioService.createUsuario(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /**
     * Actualiza un usuario existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UsuarioDTO> updateUsuario(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUsuarioRequest request
    ) {
        log.info("PUT /api/v1/usuarios/{} - Actualizar usuario", id);
        // TODO: Obtener ID del usuario autenticado desde SecurityContext
        UUID updaterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UsuarioDTO usuario = usuarioService.updateUsuario(id, request, updaterId);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Elimina un usuario (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario (soft delete)")
    public ResponseEntity<Void> deleteUsuario(@PathVariable UUID id) {
        log.info("DELETE /api/v1/usuarios/{} - Eliminar usuario", id);
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bloquea un usuario
     */
    @PostMapping("/{id}/bloquear")
    @Operation(summary = "Bloquear usuario")
    public ResponseEntity<Void> bloquearUsuario(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, Integer> body
    ) {
        log.info("POST /api/v1/usuarios/{}/bloquear - Bloquear usuario", id);
        Integer duracionSegundos = body != null ? body.get("duracionSegundos") : null;
        usuarioService.bloquearUsuario(id, duracionSegundos);
        return ResponseEntity.ok().build();
    }

    /**
     * Desbloquea un usuario
     */
    @PostMapping("/{id}/desbloquear")
    @Operation(summary = "Desbloquear usuario")
    public ResponseEntity<Void> desbloquearUsuario(@PathVariable UUID id) {
        log.info("POST /api/v1/usuarios/{}/desbloquear - Desbloquear usuario", id);
        usuarioService.desbloquearUsuario(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Restablece la contraseña de un usuario
     */
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Restablecer contraseña")
    public ResponseEntity<Void> resetPassword(
        @PathVariable UUID id,
        @RequestBody Map<String, String> body
    ) {
        log.info("POST /api/v1/usuarios/{}/reset-password - Restablecer contraseña", id);
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
        usuarioService.resetPassword(id, newPassword);
        return ResponseEntity.ok().build();
    }
}
