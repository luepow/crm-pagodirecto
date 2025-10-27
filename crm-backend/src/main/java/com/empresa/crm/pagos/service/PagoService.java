package com.empresa.crm.pagos.service;

import com.empresa.crm.pagos.dto.ActualizarEstadoPagoRequest;
import com.empresa.crm.pagos.dto.CrearPagoRequest;
import com.empresa.crm.pagos.dto.PagoDTO;
import com.empresa.crm.pagos.model.EstadoPago;
import com.empresa.crm.pagos.model.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Pago operations
 * Bounded context: Pagos
 */
public interface PagoService {

    /**
     * List all pagos with pagination
     */
    Page<PagoDTO> listar(String search, Pageable pageable);

    /**
     * Get pago by ID
     */
    Optional<PagoDTO> obtener(Long id);

    /**
     * Get pago by folio
     */
    Optional<PagoDTO> obtenerPorFolio(String folio);

    /**
     * Get pagos by venta
     */
    List<PagoDTO> obtenerPorVenta(Long ventaId);

    /**
     * Get pagos by estado
     */
    Page<PagoDTO> obtenerPorEstado(EstadoPago estado, Pageable pageable);

    /**
     * Get pagos by metodo pago
     */
    Page<PagoDTO> obtenerPorMetodoPago(MetodoPago metodoPago, Pageable pageable);

    /**
     * Get pagos by fecha range
     */
    Page<PagoDTO> obtenerPorFechaRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Create new pago
     */
    PagoDTO crear(CrearPagoRequest request);

    /**
     * Update pago estado
     */
    Optional<PagoDTO> actualizarEstado(Long id, ActualizarEstadoPagoRequest request);

    /**
     * Delete pago (soft delete)
     */
    void eliminar(Long id);

    /**
     * Generate unique folio for pago
     */
    String generarFolio();
}
