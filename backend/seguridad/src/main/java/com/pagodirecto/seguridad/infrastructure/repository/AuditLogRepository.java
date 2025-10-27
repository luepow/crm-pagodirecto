package com.pagodirecto.seguridad.infrastructure.repository;

import com.pagodirecto.seguridad.domain.AuditLog;
import com.pagodirecto.seguridad.domain.AuditResultado;
import com.pagodirecto.seguridad.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio: AuditLogRepository
 *
 * Repositorio de acceso a datos para la entidad AuditLog
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Busca logs de auditoría por usuario con paginación
     *
     * @param usuario  el usuario
     * @param pageable configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByUsuario(Usuario usuario, Pageable pageable);

    /**
     * Busca logs de auditoría por acción
     *
     * @param accion   la acción
     * @param pageable configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByAccion(String accion, Pageable pageable);

    /**
     * Busca logs de auditoría por recurso
     *
     * @param recurso  el recurso
     * @param pageable configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByRecurso(String recurso, Pageable pageable);

    /**
     * Busca logs de auditoría por resultado
     *
     * @param resultado el resultado
     * @param pageable  configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByResultado(AuditResultado resultado, Pageable pageable);

    /**
     * Busca logs de auditoría en un rango de fechas
     *
     * @param desde    fecha inicial
     * @param hasta    fecha final
     * @param pageable configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByCreatedAtBetween(Instant desde, Instant hasta, Pageable pageable);

    /**
     * Busca logs de auditoría por usuario y acción
     *
     * @param usuario  el usuario
     * @param accion   la acción
     * @param pageable configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByUsuarioAndAccion(Usuario usuario, String accion, Pageable pageable);

    /**
     * Busca logs de auditoría por recurso y recurso ID
     *
     * @param recurso   el recurso
     * @param recursoId el ID del recurso
     * @param pageable  configuración de paginación
     * @return página de logs
     */
    Page<AuditLog> findByRecursoAndRecursoId(String recurso, UUID recursoId, Pageable pageable);

    /**
     * Busca intentos fallidos de login recientes
     *
     * @param accion   la acción (típicamente "LOGIN")
     * @param desde    fecha desde
     * @param pageable configuración de paginación
     * @return página de logs
     */
    @Query("SELECT al FROM AuditLog al WHERE al.accion = :accion " +
           "AND al.resultado = 'FAILURE' AND al.createdAt >= :desde " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findFailedLoginAttempts(@Param("accion") String accion,
                                           @Param("desde") Instant desde,
                                           Pageable pageable);

    /**
     * Cuenta acciones de un usuario en un período
     *
     * @param usuario el usuario
     * @param desde   fecha inicial
     * @param hasta   fecha final
     * @return número de acciones
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.usuario = :usuario " +
           "AND al.createdAt BETWEEN :desde AND :hasta")
    long countByUsuarioAndPeriod(@Param("usuario") Usuario usuario,
                                  @Param("desde") Instant desde,
                                  @Param("hasta") Instant hasta);

    /**
     * Busca actividades sospechosas (múltiples fallos desde la misma IP)
     *
     * @param ipAddress dirección IP
     * @param desde     fecha desde
     * @param minFallos número mínimo de fallos
     * @return lista de logs
     */
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress " +
           "AND al.resultado = 'FAILURE' AND al.createdAt >= :desde " +
           "GROUP BY al.ipAddress HAVING COUNT(al) >= :minFallos")
    List<AuditLog> findSuspiciousActivity(@Param("ipAddress") String ipAddress,
                                          @Param("desde") Instant desde,
                                          @Param("minFallos") long minFallos);
}
