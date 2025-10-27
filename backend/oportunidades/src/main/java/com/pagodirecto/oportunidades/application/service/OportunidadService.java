package com.pagodirecto.oportunidades.application.service;

import com.pagodirecto.oportunidades.application.dto.OportunidadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Servicio: Oportunidad
 *
 * Interface de servicio para operaciones de negocio relacionadas con oportunidades.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface OportunidadService {

    /**
     * Crea una nueva oportunidad
     *
     * @param oportunidadDTO datos de la oportunidad
     * @param usuarioId UUID del usuario que crea el registro
     * @return DTO de la oportunidad creada
     */
    OportunidadDTO crear(OportunidadDTO oportunidadDTO, UUID usuarioId);

    /**
     * Actualiza una oportunidad existente
     *
     * @param id UUID de la oportunidad
     * @param oportunidadDTO datos actualizados
     * @param usuarioId UUID del usuario que actualiza
     * @return DTO de la oportunidad actualizada
     */
    OportunidadDTO actualizar(UUID id, OportunidadDTO oportunidadDTO, UUID usuarioId);

    /**
     * Busca una oportunidad por ID
     *
     * @param id UUID de la oportunidad
     * @return DTO de la oportunidad
     */
    OportunidadDTO buscarPorId(UUID id);

    /**
     * Lista todas las oportunidades con paginación
     *
     * @param pageable configuración de paginación
     * @return página de oportunidades
     */
    Page<OportunidadDTO> listarTodas(Pageable pageable);

    /**
     * Busca oportunidades por cliente
     *
     * @param clienteId UUID del cliente
     * @param pageable configuración de paginación
     * @return página de oportunidades
     */
    Page<OportunidadDTO> buscarPorCliente(UUID clienteId, Pageable pageable);

    /**
     * Busca oportunidades por etapa
     *
     * @param etapaId UUID de la etapa
     * @param pageable configuración de paginación
     * @return página de oportunidades
     */
    Page<OportunidadDTO> buscarPorEtapa(UUID etapaId, Pageable pageable);

    /**
     * Busca oportunidades por propietario
     *
     * @param propietarioId UUID del propietario
     * @param pageable configuración de paginación
     * @return página de oportunidades
     */
    Page<OportunidadDTO> buscarPorPropietario(UUID propietarioId, Pageable pageable);

    /**
     * Busca oportunidades por término de búsqueda
     *
     * @param searchTerm término de búsqueda
     * @param pageable configuración de paginación
     * @return página de oportunidades
     */
    Page<OportunidadDTO> buscar(String searchTerm, Pageable pageable);

    /**
     * Mueve una oportunidad a una nueva etapa
     *
     * @param id UUID de la oportunidad
     * @param etapaId UUID de la nueva etapa
     * @param probabilidad nueva probabilidad de cierre
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO de la oportunidad actualizada
     */
    OportunidadDTO moverAEtapa(UUID id, UUID etapaId, java.math.BigDecimal probabilidad, UUID usuarioId);

    /**
     * Marca una oportunidad como ganada
     *
     * @param id UUID de la oportunidad
     * @param fechaCierre fecha de cierre real
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO de la oportunidad actualizada
     */
    OportunidadDTO marcarComoGanada(UUID id, LocalDate fechaCierre, UUID usuarioId);

    /**
     * Marca una oportunidad como perdida
     *
     * @param id UUID de la oportunidad
     * @param motivo motivo de la pérdida
     * @param usuarioId UUID del usuario que realiza la acción
     * @return DTO de la oportunidad actualizada
     */
    OportunidadDTO marcarComoPerdida(UUID id, String motivo, UUID usuarioId);

    /**
     * Elimina una oportunidad (soft delete)
     *
     * @param id UUID de la oportunidad
     */
    void eliminar(UUID id);

    /**
     * Cuenta oportunidades por etapa
     *
     * @param etapaId UUID de la etapa
     * @return cantidad de oportunidades
     */
    long contarPorEtapa(UUID etapaId);
}
