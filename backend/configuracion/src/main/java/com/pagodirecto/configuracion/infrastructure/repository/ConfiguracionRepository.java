package com.pagodirecto.configuracion.infrastructure.repository;

import com.pagodirecto.configuracion.domain.Configuracion;
import com.pagodirecto.configuracion.domain.ConfiguracionCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository: ConfiguracionRepository
 *
 * Repositorio para gestión de configuraciones del sistema
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, UUID> {

    /**
     * Busca una configuración por clave
     */
    Optional<Configuracion> findByClave(String clave);

    /**
     * Busca todas las configuraciones por categoría
     */
    List<Configuracion> findByCategoria(ConfiguracionCategoria categoria);

    /**
     * Busca todas las configuraciones públicas
     */
    List<Configuracion> findByEsPublicaTrue();

    /**
     * Busca todas las configuraciones modificables
     */
    List<Configuracion> findByEsModificableTrue();

    /**
     * Verifica si existe una configuración con la clave dada
     */
    boolean existsByClave(String clave);

    /**
     * Busca configuraciones por categoría y unidad de negocio
     */
    @Query("SELECT c FROM Configuracion c WHERE c.categoria = :categoria " +
           "AND (c.unidadNegocioId = :unidadNegocioId OR c.unidadNegocioId IS NULL) " +
           "ORDER BY c.nombre ASC")
    List<Configuracion> findByCategoriaAndUnidadNegocio(
        @Param("categoria") ConfiguracionCategoria categoria,
        @Param("unidadNegocioId") UUID unidadNegocioId
    );

    /**
     * Busca configuraciones globales (sin unidad de negocio asignada)
     */
    @Query("SELECT c FROM Configuracion c WHERE c.unidadNegocioId IS NULL ORDER BY c.categoria, c.nombre")
    List<Configuracion> findConfiguracionesGlobales();
}
