package com.pagodirecto.productos.application.service;

import com.pagodirecto.productos.application.dto.ProductoDTO;
import com.pagodirecto.productos.domain.ProductoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service: ProductoService
 *
 * Interfaz de servicio para gestión de productos.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface ProductoService {

    /**
     * Crea un nuevo producto
     */
    ProductoDTO crear(ProductoDTO productoDTO);

    /**
     * Actualiza un producto existente
     */
    ProductoDTO actualizar(UUID id, ProductoDTO productoDTO);

    /**
     * Busca un producto por ID
     */
    ProductoDTO buscarPorId(UUID id);

    /**
     * Busca un producto por código
     */
    ProductoDTO buscarPorCodigo(String codigo);

    /**
     * Lista todos los productos con paginación
     */
    Page<ProductoDTO> listar(Pageable pageable);

    /**
     * Lista productos por status
     */
    Page<ProductoDTO> listarPorStatus(ProductoStatus status, Pageable pageable);

    /**
     * Lista productos por categoría
     */
    Page<ProductoDTO> listarPorCategoria(UUID categoriaId, Pageable pageable);

    /**
     * Lista productos activos
     */
    Page<ProductoDTO> listarActivos(Pageable pageable);

    /**
     * Lista productos que requieren reabastecimiento
     */
    List<ProductoDTO> listarParaReabastecer();

    /**
     * Búsqueda de texto en productos
     */
    Page<ProductoDTO> buscar(String query, Pageable pageable);

    /**
     * Actualiza el stock de un producto
     */
    ProductoDTO actualizarStock(UUID id, Integer cantidad);

    /**
     * Activa un producto
     */
    ProductoDTO activar(UUID id);

    /**
     * Desactiva un producto
     */
    ProductoDTO desactivar(UUID id);

    /**
     * Descontinúa un producto
     */
    ProductoDTO descontinuar(UUID id);

    /**
     * Elimina un producto (soft delete)
     */
    void eliminar(UUID id);

    /**
     * Cuenta productos por status
     */
    long contarPorStatus(ProductoStatus status);
}
