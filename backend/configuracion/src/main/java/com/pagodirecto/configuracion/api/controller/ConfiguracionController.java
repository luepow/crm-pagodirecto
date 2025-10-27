package com.pagodirecto.configuracion.api.controller;

import com.pagodirecto.configuracion.application.dto.*;
import com.pagodirecto.configuracion.application.service.ConfiguracionService;
import com.pagodirecto.configuracion.domain.ConfiguracionCategoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller: ConfiguracionController
 *
 * API para gestión de configuraciones del sistema
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/configuracion")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuración", description = "API de gestión de configuraciones del sistema")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @GetMapping
    @Operation(summary = "Obtener todas las configuraciones")
    public ResponseEntity<List<ConfiguracionDTO>> obtenerTodas() {
        log.info("Solicitando todas las configuraciones");
        List<ConfiguracionDTO> configuraciones = configuracionService.obtenerTodas();
        return ResponseEntity.ok(configuraciones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener configuración por ID")
    public ResponseEntity<ConfiguracionDTO> obtenerPorId(@PathVariable UUID id) {
        log.info("Solicitando configuración con ID: {}", id);
        // Este método no está implementado en el servicio, pero se puede agregar
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/clave/{clave}")
    @Operation(summary = "Obtener configuración por clave")
    public ResponseEntity<ConfiguracionDTO> obtenerPorClave(@PathVariable String clave) {
        log.info("Solicitando configuración con clave: {}", clave);
        ConfiguracionDTO configuracion = configuracionService.obtenerPorClave(clave);
        return ResponseEntity.ok(configuracion);
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Obtener configuraciones por categoría")
    public ResponseEntity<List<ConfiguracionDTO>> obtenerPorCategoria(
            @PathVariable ConfiguracionCategoria categoria) {
        log.info("Solicitando configuraciones de categoría: {}", categoria);
        List<ConfiguracionDTO> configuraciones = configuracionService.obtenerPorCategoria(categoria);
        return ResponseEntity.ok(configuraciones);
    }

    @PostMapping
    @Operation(summary = "Crear nueva configuración")
    public ResponseEntity<ConfiguracionDTO> crear(
            @Valid @RequestBody ConfiguracionDTO configuracionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} creando nueva configuración", userId);

        ConfiguracionDTO created = configuracionService.crear(configuracionDTO, userId);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar configuración")
    public ResponseEntity<ConfiguracionDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ConfiguracionDTO configuracionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} actualizando configuración {}", userId, id);

        ConfiguracionDTO updated = configuracionService.actualizar(id, configuracionDTO, userId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/clave/{clave}/valor")
    @Operation(summary = "Actualizar valor de configuración por clave")
    public ResponseEntity<ConfiguracionDTO> actualizarValor(
            @PathVariable String clave,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        String valor = body.get("valor");
        log.info("Usuario {} actualizando valor de configuración {}", userId, clave);

        ConfiguracionDTO updated = configuracionService.actualizarValor(clave, valor, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar configuración")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} eliminando configuración {}", userId, id);

        configuracionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints específicos por categoría

    @GetMapping("/general")
    @Operation(summary = "Obtener configuración general")
    public ResponseEntity<ConfiguracionGeneralDTO> obtenerConfiguracionGeneral() {
        log.info("Solicitando configuración general");
        ConfiguracionGeneralDTO config = configuracionService.obtenerConfiguracionGeneral();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/general")
    @Operation(summary = "Actualizar configuración general")
    public ResponseEntity<ConfiguracionGeneralDTO> actualizarConfiguracionGeneral(
            @Valid @RequestBody ConfiguracionGeneralDTO configuracionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} actualizando configuración general", userId);

        ConfiguracionGeneralDTO updated = configuracionService.actualizarConfiguracionGeneral(configuracionDTO, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/notificaciones")
    @Operation(summary = "Obtener configuración de notificaciones")
    public ResponseEntity<ConfiguracionNotificacionesDTO> obtenerConfiguracionNotificaciones() {
        log.info("Solicitando configuración de notificaciones");
        ConfiguracionNotificacionesDTO config = configuracionService.obtenerConfiguracionNotificaciones();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/notificaciones")
    @Operation(summary = "Actualizar configuración de notificaciones")
    public ResponseEntity<ConfiguracionNotificacionesDTO> actualizarConfiguracionNotificaciones(
            @Valid @RequestBody ConfiguracionNotificacionesDTO configuracionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} actualizando configuración de notificaciones", userId);

        ConfiguracionNotificacionesDTO updated =
            configuracionService.actualizarConfiguracionNotificaciones(configuracionDTO, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/integraciones")
    @Operation(summary = "Obtener configuración de integraciones")
    public ResponseEntity<ConfiguracionIntegracionesDTO> obtenerConfiguracionIntegraciones() {
        log.info("Solicitando configuración de integraciones");
        ConfiguracionIntegracionesDTO config = configuracionService.obtenerConfiguracionIntegraciones();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/integraciones")
    @Operation(summary = "Actualizar configuración de integraciones")
    public ResponseEntity<ConfiguracionIntegracionesDTO> actualizarConfiguracionIntegraciones(
            @Valid @RequestBody ConfiguracionIntegracionesDTO configuracionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} actualizando configuración de integraciones", userId);

        ConfiguracionIntegracionesDTO updated =
            configuracionService.actualizarConfiguracionIntegraciones(configuracionDTO, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/seguridad")
    @Operation(summary = "Obtener configuración de seguridad")
    public ResponseEntity<ConfiguracionSeguridadDTO> obtenerConfiguracionSeguridad() {
        log.info("Solicitando configuración de seguridad");
        ConfiguracionSeguridadDTO config = configuracionService.obtenerConfiguracionSeguridad();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/seguridad")
    @Operation(summary = "Actualizar configuración de seguridad")
    public ResponseEntity<ConfiguracionSeguridadDTO> actualizarConfiguracionSeguridad(
            @Valid @RequestBody ConfiguracionSeguridadDTO configuracionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} actualizando configuración de seguridad", userId);

        ConfiguracionSeguridadDTO updated =
            configuracionService.actualizarConfiguracionSeguridad(configuracionDTO, userId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/inicializar")
    @Operation(summary = "Inicializar configuraciones por defecto")
    public ResponseEntity<Map<String, String>> inicializar(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserIdFromPrincipal(userDetails);
        log.info("Usuario {} inicializando configuraciones por defecto", userId);

        configuracionService.inicializarConfiguracionesPorDefecto();
        return ResponseEntity.ok(Map.of("message", "Configuraciones inicializadas exitosamente"));
    }

    private UUID getUserIdFromPrincipal(UserDetails userDetails) {
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            log.error("No se pudo extraer UUID del username: {}", userDetails.getUsername());
            throw new IllegalArgumentException("Username no es un UUID válido");
        }
    }
}
