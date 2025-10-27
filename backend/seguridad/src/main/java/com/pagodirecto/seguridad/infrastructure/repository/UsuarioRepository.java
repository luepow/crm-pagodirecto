package com.pagodirecto.seguridad.infrastructure.repository;

import com.pagodirecto.seguridad.domain.Usuario;
import com.pagodirecto.seguridad.domain.UsuarioStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio: UsuarioRepository
 *
 * Repositorio de acceso a datos para la entidad Usuario
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca un usuario por su username
     *
     * @param username el username del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su email
     *
     * @param email el email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el username dado
     *
     * @param username el username a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     *
     * @param email el email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por unidad de negocio
     *
     * @param unidadNegocioId el ID de la unidad de negocio
     * @return lista de usuarios
     */
    List<Usuario> findByUnidadNegocioId(UUID unidadNegocioId);

    /**
     * Busca usuarios por estado
     *
     * @param status el estado del usuario
     * @return lista de usuarios
     */
    List<Usuario> findByStatus(UsuarioStatus status);

    /**
     * Busca usuarios activos por unidad de negocio
     *
     * @param unidadNegocioId el ID de la unidad de negocio
     * @param status          el estado del usuario
     * @return lista de usuarios
     */
    List<Usuario> findByUnidadNegocioIdAndStatus(UUID unidadNegocioId, UsuarioStatus status);

    /**
     * Busca un usuario con sus roles cargados (evita N+1 queries)
     *
     * @param username el username del usuario
     * @return Optional con el usuario y sus roles
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Busca un usuario con sus roles y permisos cargados
     *
     * @param username el username del usuario
     * @return Optional con el usuario, roles y permisos
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permisos " +
           "WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    /**
     * Busca un usuario por ID con sus roles cargados
     *
     * @param id el ID del usuario
     * @return Optional con el usuario y sus roles
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<Usuario> findByIdWithRoles(@Param("id") UUID id);
}
