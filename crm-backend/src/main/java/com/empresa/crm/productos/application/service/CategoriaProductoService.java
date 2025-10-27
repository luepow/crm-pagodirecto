package com.empresa.crm.productos.application.service;

import com.empresa.crm.productos.application.dto.ActualizarCategoriaRequest;
import com.empresa.crm.productos.application.dto.CategoriaProductoDTO;
import com.empresa.crm.productos.application.dto.CrearCategoriaRequest;
import com.empresa.crm.productos.domain.CategoriaProducto;
import com.empresa.crm.productos.infrastructure.repository.CategoriaProductoRepository;
import com.empresa.crm.productos.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for CategoriaProducto domain.
 * Application layer - Use cases and business logic orchestration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoriaProductoService {

    private final CategoriaProductoRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    /**
     * Get all active categories
     */
    public List<CategoriaProductoDTO> getAllCategorias() {
        log.debug("Fetching all active categories");
        return categoriaRepository.findAllActive().stream()
                .map(CategoriaProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    public CategoriaProductoDTO getCategoriaById(UUID id) {
        log.debug("Fetching category with ID: {}", id);
        CategoriaProducto categoria = categoriaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Categoria not found with ID: " + id));
        return CategoriaProductoDTO.fromEntity(categoria);
    }

    /**
     * Create new category
     */
    @Transactional
    public CategoriaProductoDTO createCategoria(CrearCategoriaRequest request) {
        log.info("Creating new category: {}", request.getNombre());

        // Validate unique name
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Category already exists with name: " + request.getNombre());
        }

        CategoriaProducto categoria = CategoriaProducto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo())
                .build();

        CategoriaProducto saved = categoriaRepository.save(categoria);
        log.info("Category created successfully with ID: {}", saved.getId());

        return CategoriaProductoDTO.fromEntity(saved);
    }

    /**
     * Update category
     */
    @Transactional
    public CategoriaProductoDTO updateCategoria(UUID id, ActualizarCategoriaRequest request) {
        log.info("Updating category with ID: {}", id);

        CategoriaProducto categoria = categoriaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Categoria not found with ID: " + id));

        // Validate unique name if changed
        if (request.getNombre() != null && !request.getNombre().equals(categoria.getNombre())) {
            if (categoriaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
                throw new RuntimeException("Category already exists with name: " + request.getNombre());
            }
            categoria.setNombre(request.getNombre());
        }

        if (request.getDescripcion() != null) {
            categoria.setDescripcion(request.getDescripcion());
        }

        if (request.getActivo() != null) {
            categoria.setActivo(request.getActivo());
        }

        CategoriaProducto updated = categoriaRepository.save(categoria);
        log.info("Category updated successfully with ID: {}", id);

        return CategoriaProductoDTO.fromEntity(updated);
    }

    /**
     * Delete category (soft delete)
     */
    @Transactional
    public void deleteCategoria(UUID id) {
        log.info("Deleting category with ID: {}", id);

        CategoriaProducto categoria = categoriaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Categoria not found with ID: " + id));

        // Check if category has products
        long productCount = productoRepository.countByCategoriaId(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with " + productCount + " associated products");
        }

        categoria.softDelete();
        categoriaRepository.save(categoria);

        log.info("Category soft deleted successfully with ID: {}", id);
    }

    /**
     * Activate/deactivate category
     */
    @Transactional
    public CategoriaProductoDTO toggleActivoCategoria(UUID id, boolean activo) {
        log.info("Setting category {} activo status to: {}", id, activo);

        CategoriaProducto categoria = categoriaRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Categoria not found with ID: " + id));

        categoria.setActivo(activo);
        CategoriaProducto updated = categoriaRepository.save(categoria);

        log.info("Category activo status updated successfully");
        return CategoriaProductoDTO.fromEntity(updated);
    }
}
