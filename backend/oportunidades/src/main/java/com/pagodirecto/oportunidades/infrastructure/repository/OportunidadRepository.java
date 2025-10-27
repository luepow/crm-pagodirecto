package com.pagodirecto.oportunidades.infrastructure.repository;

import com.pagodirecto.oportunidades.domain.Oportunidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio: Oportunidad
 *
 * Proporciona acceso a datos para la entidad Oportunidad.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface OportunidadRepository extends JpaRepository<Oportunidad, UUID> {

    /**
     * Busca oportunidades por cliente
     *
     * @param clienteId UUID del cliente
     * @param pageable paginación
     * @return página de oportunidades
     */
    Page<Oportunidad> findByClienteId(UUID clienteId, Pageable pageable);

    /**
     * Busca oportunidades por etapa
     *
     * @param etapaId UUID de la etapa
     * @param pageable paginación
     * @return página de oportunidades
     */
    Page<Oportunidad> findByEtapaId(UUID etapaId, Pageable pageable);

    /**
     * Busca oportunidades por propietario
     *
     * @param propietarioId UUID del propietario
     * @param pageable paginación
     * @return página de oportunidades
     */
    Page<Oportunidad> findByPropietarioId(UUID propietarioId, Pageable pageable);

    /**
     * Busca oportunidades por unidad de negocio
     *
     * @param unidadNegocioId UUID de la unidad de negocio
     * @param pageable paginación
     * @return página de oportunidades
     */
    Page<Oportunidad> findByUnidadNegocioId(UUID unidadNegocioId, Pageable pageable);

    /**
     * Búsqueda de oportunidades por título o descripción
     *
     * @param searchTerm término de búsqueda
     * @param pageable paginación
     * @return página de oportunidades
     */
    @Query("SELECT o FROM Oportunidad o WHERE " +
           "LOWER(o.titulo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Oportunidad> searchOportunidades(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Cuenta oportunidades por etapa
     *
     * @param etapaId UUID de la etapa
     * @return cantidad de oportunidades
     */
    long countByEtapaId(UUID etapaId);

    /**
     * Busca oportunidades ganadas (probabilidad = 100)
     *
     * @param pageable paginación
     * @return página de oportunidades ganadas
     */
    @Query("SELECT o FROM Oportunidad o WHERE o.probabilidad = 100")
    Page<Oportunidad> findOportunidadesGanadas(Pageable pageable);

    /**
     * Busca oportunidades perdidas (probabilidad = 0 y motivo perdida no nulo)
     *
     * @param pageable paginación
     * @return página de oportunidades perdidas
     */
    @Query("SELECT o FROM Oportunidad o WHERE o.probabilidad = 0 AND o.motivoPerdida IS NOT NULL")
    Page<Oportunidad> findOportunidadesPerdidas(Pageable pageable);
}
