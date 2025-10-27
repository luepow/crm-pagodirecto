package com.empresa.crm.cuentas.service;

import com.empresa.crm.cuentas.dto.AplicarPagoRequest;
import com.empresa.crm.cuentas.dto.CrearCuentaRequest;
import com.empresa.crm.cuentas.dto.CuentaDTO;
import com.empresa.crm.cuentas.model.EstadoCuenta;
import com.empresa.crm.cuentas.model.TipoCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Cuenta operations
 * Bounded context: Cuentas
 */
public interface CuentaService {

    /**
     * List all cuentas with pagination
     */
    Page<CuentaDTO> listar(String search, Pageable pageable);

    /**
     * Get cuenta by ID
     */
    Optional<CuentaDTO> obtener(Long id);

    /**
     * Get cuenta by folio
     */
    Optional<CuentaDTO> obtenerPorFolio(String folio);

    /**
     * Get cuentas by tipo
     */
    Page<CuentaDTO> obtenerPorTipo(TipoCuenta tipo, Pageable pageable);

    /**
     * Get cuentas by estado
     */
    Page<CuentaDTO> obtenerPorEstado(EstadoCuenta estado, Pageable pageable);

    /**
     * Get cuentas by tipo and estado
     */
    Page<CuentaDTO> obtenerPorTipoYEstado(TipoCuenta tipo, EstadoCuenta estado, Pageable pageable);

    /**
     * Get cuentas by cliente
     */
    Page<CuentaDTO> obtenerPorCliente(Long clienteId, Pageable pageable);

    /**
     * Get cuentas vencidas
     */
    Page<CuentaDTO> obtenerVencidas(Pageable pageable);

    /**
     * Get cuentas vencidas by tipo
     */
    Page<CuentaDTO> obtenerVencidasPorTipo(TipoCuenta tipo, Pageable pageable);

    /**
     * Get cuentas by referencia
     */
    List<CuentaDTO> obtenerPorReferencia(String referenciaTipo, Long referenciaId);

    /**
     * Create new cuenta
     */
    CuentaDTO crear(CrearCuentaRequest request);

    /**
     * Create cuenta from venta
     */
    CuentaDTO crearDesdeVenta(Long ventaId);

    /**
     * Aplicar pago to cuenta
     */
    Optional<CuentaDTO> aplicarPago(Long id, AplicarPagoRequest request);

    /**
     * Marcar cuenta as pagada
     */
    Optional<CuentaDTO> marcarPagada(Long id);

    /**
     * Cancelar cuenta
     */
    Optional<CuentaDTO> cancelar(Long id);

    /**
     * Delete cuenta (soft delete)
     */
    void eliminar(Long id);

    /**
     * Generate unique folio for cuenta
     */
    String generarFolio(TipoCuenta tipo);

    /**
     * Process cuentas vencidas (update estado)
     */
    void procesarCuentasVencidas();
}
