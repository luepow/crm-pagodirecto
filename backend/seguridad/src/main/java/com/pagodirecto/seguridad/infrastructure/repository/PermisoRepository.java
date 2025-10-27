package com.pagodirecto.seguridad.infrastructure.repository;

import com.pagodirecto.seguridad.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio: PermisoRepository
 *
 * Repositorio de acceso a datos para la entidad Permiso
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, UUID> {

    /**
     * Busca permisos por recurso
     */
    List<Permiso> findByRecurso(String recurso);

    /**
     * Busca permisos por acción
     */
    List<Permiso> findByAccion(String accion);

    /**
     * Busca un permiso por recurso y acción
     */
    Optional<Permiso> findByRecursoAndAccion(String recurso, String accion);

    /**
     * Verifica si existe un permiso con el recurso y acción dados
     */
    boolean existsByRecursoAndAccion(String recurso, String accion);
}
