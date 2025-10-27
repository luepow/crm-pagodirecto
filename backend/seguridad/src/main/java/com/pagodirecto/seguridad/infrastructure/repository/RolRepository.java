package com.pagodirecto.seguridad.infrastructure.repository;

import com.pagodirecto.seguridad.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio: RolRepository
 *
 * Repositorio de acceso a datos para la entidad Rol
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

    /**
     * Busca roles por unidad de negocio
     *
     * @param unidadNegocioId el ID de la unidad de negocio
     * @return lista de roles
     */
    List<Rol> findByUnidadNegocioId(UUID unidadNegocioId);

    /**
     * Busca roles por departamento
     *
     * @param departamento el nombre del departamento
     * @return lista de roles
     */
    List<Rol> findByDepartamento(String departamento);
}
