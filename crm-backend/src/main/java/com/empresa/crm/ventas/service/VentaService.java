package com.empresa.crm.ventas.service;

import com.empresa.crm.ventas.dto.ActualizarEstadoVentaRequest;
import com.empresa.crm.ventas.dto.CrearVentaRequest;
import com.empresa.crm.ventas.dto.VentaDTO;
import com.empresa.crm.ventas.model.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service interface for Venta operations
 * Bounded context: Ventas
 */
public interface VentaService {

    /**
     * List all ventas with pagination
     */
    Page<VentaDTO> listar(String search, Pageable pageable);

    /**
     * Get venta by ID
     */
    Optional<VentaDTO> obtener(Long id);

    /**
     * Get venta by folio
     */
    Optional<VentaDTO> obtenerPorFolio(String folio);

    /**
     * Get ventas by cliente
     */
    Page<VentaDTO> obtenerPorCliente(Long clienteId, Pageable pageable);

    /**
     * Get ventas by estado
     */
    Page<VentaDTO> obtenerPorEstado(EstadoVenta estado, Pageable pageable);

    /**
     * Get ventas by fecha range
     */
    Page<VentaDTO> obtenerPorFechaRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Create new venta
     */
    VentaDTO crear(CrearVentaRequest request);

    /**
     * Update venta estado
     */
    Optional<VentaDTO> actualizarEstado(Long id, ActualizarEstadoVentaRequest request);

    /**
     * Cancel venta
     */
    Optional<VentaDTO> cancelar(Long id);

    /**
     * Delete venta (soft delete)
     */
    void eliminar(Long id);

    /**
     * Generate unique folio for venta
     */
    String generarFolio();
}
