package com.empresa.crm.productos.presentation.controller;

import com.empresa.crm.productos.application.dto.ActualizarCategoriaRequest;
import com.empresa.crm.productos.application.dto.CategoriaProductoDTO;
import com.empresa.crm.productos.application.dto.CrearCategoriaRequest;
import com.empresa.crm.productos.application.service.CategoriaProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Product Categories.
 * Presentation layer - Hexagonal architecture
 *
 * Security:
 * - ADMIN: Full access
 * - MANAGER: Create/update categories
 * - USER: Read only
 */
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoriaProductoController {

    private final CategoriaProductoService categoriaService;

    /**
     * Get all active categories
     * Access: All authenticated users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CategoriaProductoDTO>> getAllCategorias() {
        log.info("GET /api/v1/categorias - Fetching all categories");
        List<CategoriaProductoDTO> categorias = categoriaService.getAllCategorias();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Get category by ID
     * Access: All authenticated users
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<CategoriaProductoDTO> getCategoriaById(@PathVariable UUID id) {
        log.info("GET /api/v1/categorias/{} - Fetching category", id);
        CategoriaProductoDTO categoria = categoriaService.getCategoriaById(id);
        return ResponseEntity.ok(categoria);
    }

    /**
     * Create new category
     * Access: MANAGER, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CategoriaProductoDTO> createCategoria(
            @Valid @RequestBody CrearCategoriaRequest request) {
        log.info("POST /api/v1/categorias - Creating category: {}", request.getNombre());
        CategoriaProductoDTO created = categoriaService.createCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update category
     * Access: MANAGER, ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CategoriaProductoDTO> updateCategoria(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarCategoriaRequest request) {
        log.info("PUT /api/v1/categorias/{} - Updating category", id);
        CategoriaProductoDTO updated = categoriaService.updateCategoria(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete category (soft delete)
     * Access: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategoria(@PathVariable UUID id) {
        log.info("DELETE /api/v1/categorias/{} - Deleting category", id);
        categoriaService.deleteCategoria(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate category
     * Access: MANAGER, ADMIN
     */
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CategoriaProductoDTO> activarCategoria(@PathVariable UUID id) {
        log.info("PATCH /api/v1/categorias/{}/activar - Activating category", id);
        CategoriaProductoDTO updated = categoriaService.toggleActivoCategoria(id, true);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deactivate category
     * Access: MANAGER, ADMIN
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<CategoriaProductoDTO> desactivarCategoria(@PathVariable UUID id) {
        log.info("PATCH /api/v1/categorias/{}/desactivar - Deactivating category", id);
        CategoriaProductoDTO updated = categoriaService.toggleActivoCategoria(id, false);
        return ResponseEntity.ok(updated);
    }
}
