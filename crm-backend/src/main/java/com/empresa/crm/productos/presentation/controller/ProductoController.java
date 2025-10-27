package com.empresa.crm.productos.presentation.controller;

import com.empresa.crm.productos.application.dto.ActualizarProductoRequest;
import com.empresa.crm.productos.application.dto.AjustarStockRequest;
import com.empresa.crm.productos.application.dto.CrearProductoRequest;
import com.empresa.crm.productos.application.dto.ProductoDTO;
import com.empresa.crm.productos.application.service.ProductoService;
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
 * REST Controller for Products.
 * Presentation layer - Hexagonal architecture
 *
 * Security:
 * - ADMIN: Full access
 * - MANAGER: Create/update products
 * - USER: Read only
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Get all active products
     * Access: All authenticated users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ProductoDTO>> getAllProductos() {
        log.info("GET /api/v1/productos - Fetching all products");
        List<ProductoDTO> productos = productoService.getAllProductos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Get product by ID
     * Access: All authenticated users
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable UUID id) {
        log.info("GET /api/v1/productos/{} - Fetching product", id);
        ProductoDTO producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    /**
     * Get product by codigo
     * Access: All authenticated users
     */
    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> getProductoByCodigo(@PathVariable String codigo) {
        log.info("GET /api/v1/productos/codigo/{} - Fetching product by code", codigo);
        ProductoDTO producto = productoService.getProductoByCodigo(codigo);
        return ResponseEntity.ok(producto);
    }

    /**
     * Search products by name
     * Access: All authenticated users
     */
    @GetMapping("/buscar/nombre")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ProductoDTO>> searchByNombre(@RequestParam String nombre) {
        log.info("GET /api/v1/productos/buscar/nombre?nombre={} - Searching products", nombre);
        List<ProductoDTO> productos = productoService.searchProductosByNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    /**
     * Get products by category
     * Access: All authenticated users
     */
    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ProductoDTO>> getProductosByCategoria(@PathVariable UUID categoriaId) {
        log.info("GET /api/v1/productos/categoria/{} - Fetching products by category", categoriaId);
        List<ProductoDTO> productos = productoService.getProductosByCategoria(categoriaId);
        return ResponseEntity.ok(productos);
    }

    /**
     * Get products with low stock
     * Access: All authenticated users
     */
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ProductoDTO>> getProductosConStockBajo() {
        log.info("GET /api/v1/productos/stock-bajo - Fetching products with low stock");
        List<ProductoDTO> productos = productoService.getProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }

    /**
     * Advanced search with multiple filters
     * Access: All authenticated users
     * @param nombre Optional: partial name match
     * @param categoriaId Optional: category filter
     * @param activo Optional: active status filter
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ProductoDTO>> searchProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) Boolean activo) {
        log.info("GET /api/v1/productos/buscar - Advanced search: nombre={}, categoria={}, activo={}",
                 nombre, categoriaId, activo);
        List<ProductoDTO> productos = productoService.searchProductos(nombre, categoriaId, activo);
        return ResponseEntity.ok(productos);
    }

    /**
     * Create new product
     * Access: MANAGER, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> createProducto(
            @Valid @RequestBody CrearProductoRequest request) {
        log.info("POST /api/v1/productos - Creating product: {}", request.getCodigo());
        ProductoDTO created = productoService.createProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update product
     * Access: MANAGER, ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> updateProducto(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarProductoRequest request) {
        log.info("PUT /api/v1/productos/{} - Updating product", id);
        ProductoDTO updated = productoService.updateProducto(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete product (soft delete)
     * Access: ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProducto(@PathVariable UUID id) {
        log.info("DELETE /api/v1/productos/{} - Deleting product", id);
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adjust product stock
     * Access: MANAGER, ADMIN
     */
    @PatchMapping("/{id}/ajustar-stock")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> ajustarStock(
            @PathVariable UUID id,
            @Valid @RequestBody AjustarStockRequest request) {
        log.info("PATCH /api/v1/productos/{}/ajustar-stock - Adjusting stock by {} units",
                 id, request.getCantidad());
        ProductoDTO updated = productoService.ajustarStock(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Activate product
     * Access: MANAGER, ADMIN
     */
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> activarProducto(@PathVariable UUID id) {
        log.info("PATCH /api/v1/productos/{}/activar - Activating product", id);
        ProductoDTO updated = productoService.toggleActivoProducto(id, true);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deactivate product
     * Access: MANAGER, ADMIN
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProductoDTO> desactivarProducto(@PathVariable UUID id) {
        log.info("PATCH /api/v1/productos/{}/desactivar - Deactivating product", id);
        ProductoDTO updated = productoService.toggleActivoProducto(id, false);
        return ResponseEntity.ok(updated);
    }
}
