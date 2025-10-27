package com.empresa.crm.productos.application.service;

import com.empresa.crm.productos.application.dto.ActualizarProductoRequest;
import com.empresa.crm.productos.application.dto.AjustarStockRequest;
import com.empresa.crm.productos.application.dto.CrearProductoRequest;
import com.empresa.crm.productos.application.dto.ProductoDTO;
import com.empresa.crm.productos.domain.CategoriaProducto;
import com.empresa.crm.productos.domain.Producto;
import com.empresa.crm.productos.infrastructure.repository.CategoriaProductoRepository;
import com.empresa.crm.productos.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Producto domain.
 * Application layer - Use cases and business logic orchestration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaRepository;

    /**
     * Get all active products
     */
    public List<ProductoDTO> getAllProductos() {
        log.debug("Fetching all active products");
        return productoRepository.findAllActive().stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    public ProductoDTO getProductoById(UUID id) {
        log.debug("Fetching product with ID: {}", id);
        Producto producto = productoRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Producto not found with ID: " + id));
        return ProductoDTO.fromEntity(producto);
    }

    /**
     * Get product by codigo
     */
    public ProductoDTO getProductoByCodigo(String codigo) {
        log.debug("Fetching product with codigo: {}", codigo);
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Producto not found with codigo: " + codigo));
        return ProductoDTO.fromEntity(producto);
    }

    /**
     * Search products by name
     */
    public List<ProductoDTO> searchProductosByNombre(String nombre) {
        log.debug("Searching products by name: {}", nombre);
        return productoRepository.findByNombreContaining(nombre).stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category
     */
    public List<ProductoDTO> getProductosByCategoria(UUID categoriaId) {
        log.debug("Fetching products for category: {}", categoriaId);
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get products with low stock
     */
    public List<ProductoDTO> getProductosConStockBajo() {
        log.debug("Fetching products with low stock");
        return productoRepository.findProductosConStockBajo().stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Advanced search with multiple filters
     */
    public List<ProductoDTO> searchProductos(String nombre, UUID categoriaId, Boolean activo) {
        log.debug("Searching products with filters - nombre: {}, categoria: {}, activo: {}",
                  nombre, categoriaId, activo);
        return productoRepository.searchProductos(nombre, categoriaId, activo).stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductoDTO createProducto(CrearProductoRequest request) {
        log.info("Creating new product: {}", request.getCodigo());

        // Validate unique codigo
        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new RuntimeException("Product already exists with codigo: " + request.getCodigo());
        }

        // Validate category exists
        CategoriaProducto categoria = categoriaRepository.findByIdAndNotDeleted(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria not found with ID: " + request.getCategoriaId()));

        // Get current user ID from security context
        UUID currentUserId = getCurrentUserId();

        Producto producto = Producto.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(categoria)
                .precio(request.getPrecio())
                .costo(request.getCosto())
                .stock(request.getStock())
                .stockMinimo(request.getStockMinimo())
                .unidadMedida(request.getUnidadMedida())
                .activo(request.getActivo())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        // Validate business rules
        producto.validate();

        Producto saved = productoRepository.save(producto);
        log.info("Product created successfully with ID: {}", saved.getId());

        return ProductoDTO.fromEntity(saved);
    }

    /**
     * Update product
     */
    @Transactional
    public ProductoDTO updateProducto(UUID id, ActualizarProductoRequest request) {
        log.info("Updating product with ID: {}", id);

        Producto producto = productoRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Producto not found with ID: " + id));

        // Validate unique codigo if changed
        if (request.getCodigo() != null && !request.getCodigo().equals(producto.getCodigo())) {
            if (productoRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
                throw new RuntimeException("Product already exists with codigo: " + request.getCodigo());
            }
            producto.setCodigo(request.getCodigo());
        }

        if (request.getNombre() != null) {
            producto.setNombre(request.getNombre());
        }

        if (request.getDescripcion() != null) {
            producto.setDescripcion(request.getDescripcion());
        }

        if (request.getCategoriaId() != null) {
            CategoriaProducto categoria = categoriaRepository.findByIdAndNotDeleted(request.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria not found with ID: " + request.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        if (request.getPrecio() != null) {
            producto.setPrecio(request.getPrecio());
        }

        if (request.getCosto() != null) {
            producto.setCosto(request.getCosto());
        }

        if (request.getStock() != null) {
            producto.setStock(request.getStock());
        }

        if (request.getStockMinimo() != null) {
            producto.setStockMinimo(request.getStockMinimo());
        }

        if (request.getUnidadMedida() != null) {
            producto.setUnidadMedida(request.getUnidadMedida());
        }

        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        }

        // Update audit field
        producto.setUpdatedBy(getCurrentUserId());

        // Validate business rules
        producto.validate();

        Producto updated = productoRepository.save(producto);
        log.info("Product updated successfully with ID: {}", id);

        return ProductoDTO.fromEntity(updated);
    }

    /**
     * Delete product (soft delete)
     */
    @Transactional
    public void deleteProducto(UUID id) {
        log.info("Deleting product with ID: {}", id);

        Producto producto = productoRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Producto not found with ID: " + id));

        producto.softDelete();
        producto.setUpdatedBy(getCurrentUserId());
        productoRepository.save(producto);

        log.info("Product soft deleted successfully with ID: {}", id);
    }

    /**
     * Adjust product stock
     */
    @Transactional
    public ProductoDTO ajustarStock(UUID id, AjustarStockRequest request) {
        log.info("Adjusting stock for product ID: {} by {} units", id, request.getCantidad());

        Producto producto = productoRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Producto not found with ID: " + id));

        try {
            producto.adjustStock(request.getCantidad());
            producto.setUpdatedBy(getCurrentUserId());

            Producto updated = productoRepository.save(producto);
            log.info("Stock adjusted successfully. New stock: {}", updated.getStock());

            return ProductoDTO.fromEntity(updated);
        } catch (IllegalArgumentException e) {
            log.error("Invalid stock adjustment: {}", e.getMessage());
            throw new RuntimeException("Invalid stock adjustment: " + e.getMessage());
        }
    }

    /**
     * Activate/deactivate product
     */
    @Transactional
    public ProductoDTO toggleActivoProducto(UUID id, boolean activo) {
        log.info("Setting product {} activo status to: {}", id, activo);

        Producto producto = productoRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Producto not found with ID: " + id));

        producto.setActivo(activo);
        producto.setUpdatedBy(getCurrentUserId());
        Producto updated = productoRepository.save(producto);

        log.info("Product activo status updated successfully");
        return ProductoDTO.fromEntity(updated);
    }

    /**
     * Helper method to get current user ID from security context
     * In production, this should extract the user ID from JWT or session
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {
            // Extract user ID from authentication principal
            // This is a placeholder - implement based on your security configuration
            return UUID.randomUUID(); // TODO: Extract from JWT or User principal
        }
        return UUID.randomUUID(); // Default for anonymous/system operations
    }
}
