package com.pagodirecto.productos.api.controller;

import com.pagodirecto.productos.application.dto.ProductoDTO;
import com.pagodirecto.productos.application.service.ProductoService;
import com.pagodirecto.productos.domain.ProductoStatus;
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
 * REST Controller: Productos
 *
 * Endpoints para gestión del catálogo de productos.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/productos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "API de gestión de catálogo de productos")
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    @Operation(summary = "Crear nuevo producto")
    public ResponseEntity<ProductoDTO> crear(
            @Valid @RequestBody ProductoDTO productoDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para crear producto: {} por usuario: {}",
                productoDTO.getNombre(), userDetails.getUsername());

        ProductoDTO productoCreado = productoService.crear(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto existente")
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProductoDTO productoDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para actualizar producto {} por usuario: {}",
                id, userDetails.getUsername());

        ProductoDTO productoActualizado = productoService.actualizar(id, productoDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable UUID id) {
        log.debug("Solicitud para obtener producto con ID: {}", id);
        ProductoDTO producto = productoService.buscarPorId(id);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Obtener producto por código")
    public ResponseEntity<ProductoDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.debug("Solicitud para obtener producto con código: {}", codigo);
        ProductoDTO producto = productoService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(producto);
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos con paginación")
    public ResponseEntity<Page<ProductoDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre,asc") String[] sort) {
        log.debug("Solicitud para listar productos - página: {}, tamaño: {}", page, size);

        Pageable pageable = crearPageable(page, size, sort);
        Page<ProductoDTO> productos = productoService.listar(pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar productos por status")
    public ResponseEntity<Page<ProductoDTO>> listarPorStatus(
            @PathVariable ProductoStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre,asc") String[] sort) {
        log.debug("Solicitud para listar productos con status: {}", status);

        Pageable pageable = crearPageable(page, size, sort);
        Page<ProductoDTO> productos = productoService.listarPorStatus(status, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar productos por categoría")
    public ResponseEntity<Page<ProductoDTO>> listarPorCategoria(
            @PathVariable UUID categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre,asc") String[] sort) {
        log.debug("Solicitud para listar productos de categoría: {}", categoriaId);

        Pageable pageable = crearPageable(page, size, sort);
        Page<ProductoDTO> productos = productoService.listarPorCategoria(categoriaId, pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar productos activos")
    public ResponseEntity<Page<ProductoDTO>> listarActivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre,asc") String[] sort) {
        log.debug("Solicitud para listar productos activos");

        Pageable pageable = crearPageable(page, size, sort);
        Page<ProductoDTO> productos = productoService.listarActivos(pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/reabastecer")
    @Operation(summary = "Listar productos que requieren reabastecimiento")
    public ResponseEntity<List<ProductoDTO>> listarParaReabastecer() {
        log.debug("Solicitud para listar productos para reabastecer");

        List<ProductoDTO> productos = productoService.listarParaReabastecer();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar productos por texto")
    public ResponseEntity<Page<ProductoDTO>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre,asc") String[] sort) {
        log.debug("Solicitud de búsqueda de productos con query: {}", q);

        Pageable pageable = crearPageable(page, size, sort);
        Page<ProductoDTO> productos = productoService.buscar(q, pageable);
        return ResponseEntity.ok(productos);
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Actualizar stock del producto")
    public ResponseEntity<ProductoDTO> actualizarStock(
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer cantidad = body.get("cantidad");
        log.info("Solicitud para actualizar stock del producto {} en {} unidades por usuario: {}",
                id, cantidad, userDetails.getUsername());

        ProductoDTO productoActualizado = productoService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(productoActualizado);
    }

    @PutMapping("/{id}/activar")
    @Operation(summary = "Activar producto")
    public ResponseEntity<ProductoDTO> activar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para activar producto {} por usuario: {}",
                id, userDetails.getUsername());

        ProductoDTO productoActivado = productoService.activar(id);
        return ResponseEntity.ok(productoActivado);
    }

    @PutMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar producto")
    public ResponseEntity<ProductoDTO> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para desactivar producto {} por usuario: {}",
                id, userDetails.getUsername());

        ProductoDTO productoDesactivado = productoService.desactivar(id);
        return ResponseEntity.ok(productoDesactivado);
    }

    @PutMapping("/{id}/descontinuar")
    @Operation(summary = "Descontinuar producto")
    public ResponseEntity<ProductoDTO> descontinuar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para descontinuar producto {} por usuario: {}",
                id, userDetails.getUsername());

        ProductoDTO productoDescontinuado = productoService.descontinuar(id);
        return ResponseEntity.ok(productoDescontinuado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto (soft delete)")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Solicitud para eliminar producto {} por usuario: {}",
                id, userDetails.getUsername());

        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/status/{status}")
    @Operation(summary = "Contar productos por status")
    public ResponseEntity<Long> contarPorStatus(@PathVariable ProductoStatus status) {
        log.debug("Solicitud para contar productos con status: {}", status);

        long count = productoService.contarPorStatus(status);
        return ResponseEntity.ok(count);
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
