package com.pagodirecto.productos.application.service.impl;

import com.pagodirecto.productos.application.dto.ProductoDTO;
import com.pagodirecto.productos.application.mapper.ProductoMapper;
import com.pagodirecto.productos.application.service.ProductoService;
import com.pagodirecto.productos.domain.Producto;
import com.pagodirecto.productos.domain.ProductoStatus;
import com.pagodirecto.productos.infrastructure.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service Implementation: ProductoServiceImpl
 *
 * Implementación del servicio de gestión de productos.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Override
    public ProductoDTO crear(ProductoDTO productoDTO) {
        log.info("Creando nuevo producto: {}", productoDTO.getNombre());

        if (productoRepository.existsByCodigo(productoDTO.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un producto con el código: " + productoDTO.getCodigo());
        }

        Producto producto = productoMapper.toEntity(productoDTO);
        producto.setCreatedAt(Instant.now());
        producto.setUpdatedAt(Instant.now());

        Producto productoGuardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());

        return productoMapper.toDTO(productoGuardado);
    }

    @Override
    public ProductoDTO actualizar(UUID id, ProductoDTO productoDTO) {
        log.info("Actualizando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        productoMapper.updateEntityFromDTO(productoDTO, producto);
        producto.setUpdatedAt(Instant.now());

        Producto productoActualizado = productoRepository.save(producto);
        log.info("Producto actualizado exitosamente: {}", id);

        return productoMapper.toDTO(productoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(UUID id) {
        log.debug("Buscando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        return productoMapper.toDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO buscarPorCodigo(String codigo) {
        log.debug("Buscando producto con código: {}", codigo);

        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con código: " + codigo));

        return productoMapper.toDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listar(Pageable pageable) {
        log.debug("Listando todos los productos con paginación");
        return productoRepository.findAll(pageable)
                .map(productoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarPorStatus(ProductoStatus status, Pageable pageable) {
        log.debug("Listando productos con status: {}", status);
        return productoRepository.findByStatus(status, pageable)
                .map(productoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarPorCategoria(UUID categoriaId, Pageable pageable) {
        log.debug("Listando productos de categoría: {}", categoriaId);
        return productoRepository.findByCategoriaId(categoriaId, pageable)
                .map(productoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarActivos(Pageable pageable) {
        log.debug("Listando productos activos");
        return productoRepository.findProductosActivos(pageable)
                .map(productoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarParaReabastecer() {
        log.debug("Listando productos que requieren reabastecimiento");
        return productoRepository.findProductosParaReabastecer()
                .stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscar(String query, Pageable pageable) {
        log.debug("Buscando productos con query: {}", query);
        return productoRepository.searchByText(query, pageable)
                .map(productoMapper::toDTO);
    }

    @Override
    public ProductoDTO actualizarStock(UUID id, Integer cantidad) {
        log.info("Actualizando stock del producto {} en {} unidades", id, cantidad);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        producto.actualizarStock(cantidad);
        Producto productoActualizado = productoRepository.save(producto);

        log.info("Stock actualizado. Nuevo stock: {}", productoActualizado.getStockActual());
        return productoMapper.toDTO(productoActualizado);
    }

    @Override
    public ProductoDTO activar(UUID id) {
        log.info("Activando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        producto.activar();
        Producto productoActivado = productoRepository.save(producto);

        log.info("Producto activado exitosamente: {}", id);
        return productoMapper.toDTO(productoActivado);
    }

    @Override
    public ProductoDTO desactivar(UUID id) {
        log.info("Desactivando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        producto.desactivar();
        Producto productoDesactivado = productoRepository.save(producto);

        log.info("Producto desactivado exitosamente: {}", id);
        return productoMapper.toDTO(productoDesactivado);
    }

    @Override
    public ProductoDTO descontinuar(UUID id) {
        log.info("Descontinuando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        producto.descontinuar();
        Producto productoDescontinuado = productoRepository.save(producto);

        log.info("Producto descontinuado exitosamente: {}", id);
        return productoMapper.toDTO(productoDescontinuado);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando producto con ID: {}", id);

        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + id);
        }

        productoRepository.deleteById(id);
        log.info("Producto eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorStatus(ProductoStatus status) {
        log.debug("Contando productos con status: {}", status);
        return productoRepository.countByStatus(status);
    }
}
