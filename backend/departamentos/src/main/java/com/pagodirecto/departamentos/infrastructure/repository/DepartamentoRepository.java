package com.pagodirecto.departamentos.infrastructure.repository;

import com.pagodirecto.departamentos.domain.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio: DepartamentoRepository
 *
 * Repositorio de acceso a datos para la entidad Departamento
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, UUID> {

    /**
     * Busca un departamento por su código
     */
    Optional<Departamento> findByCodigo(String codigo);

    /**
     * Verifica si existe un departamento con el código dado
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca departamentos por unidad de negocio
     */
    List<Departamento> findByUnidadNegocioId(UUID unidadNegocioId);

    /**
     * Busca departamentos activos por unidad de negocio
     */
    List<Departamento> findByUnidadNegocioIdAndActivoTrue(UUID unidadNegocioId);

    /**
     * Busca departamentos hijos (sub-departamentos)
     */
    List<Departamento> findByParentId(UUID parentId);

    /**
     * Busca departamentos raíz (sin padre)
     */
    List<Departamento> findByParentIdIsNull();

    /**
     * Busca departamentos por jefe
     */
    List<Departamento> findByJefeId(UUID jefeId);

    /**
     * Busca departamentos por nivel jerárquico
     */
    List<Departamento> findByNivel(Integer nivel);

    /**
     * Cuenta sub-departamentos de un departamento
     */
    @Query("SELECT COUNT(d) FROM Departamento d WHERE d.parentId = :parentId")
    Long countByParentId(@Param("parentId") UUID parentId);
}
