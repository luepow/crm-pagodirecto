package com.pagodirecto.spidi.infrastructure.repository;

import com.pagodirecto.spidi.domain.Session;
import com.pagodirecto.spidi.domain.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Session
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Busca una sesión por ID de cliente
     *
     * @param clientId ID del cliente
     * @return Optional con la sesión si existe
     */
    Optional<Session> findByClientIdAndStatus(String clientId, SessionStatus status);

    /**
     * Busca sesiones activas por sala
     *
     * @param roomId ID de la sala
     * @param status estado de la sesión
     * @return lista de sesiones activas en la sala
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.room.id = :roomId
        AND s.status = :status
        ORDER BY s.startedAt DESC
    """)
    List<Session> findByRoomIdAndStatus(
        @Param("roomId") UUID roomId,
        @Param("status") SessionStatus status
    );

    /**
     * Busca sesiones de un usuario
     *
     * @param userId ID del usuario
     * @return lista de sesiones del usuario
     */
    List<Session> findByUserId(UUID userId);

    /**
     * Busca sesiones expiradas (sin heartbeat reciente)
     *
     * @param threshold timestamp de umbral
     * @return lista de sesiones expiradas
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.lastHeartbeatAt < :threshold
        AND s.status = 'ACTIVE'
    """)
    List<Session> findExpiredSessions(@Param("threshold") Instant threshold);

    /**
     * Cuenta sesiones activas por sala
     *
     * @param roomId ID de la sala
     * @return número de sesiones activas
     */
    @Query("""
        SELECT COUNT(s) FROM Session s
        WHERE s.room.id = :roomId
        AND s.status = 'ACTIVE'
    """)
    Long countActiveByRoomId(@Param("roomId") UUID roomId);

    /**
     * Calcula latencia promedio de sesiones activas en una sala
     *
     * @param roomId ID de la sala
     * @return latencia promedio en milisegundos
     */
    @Query("""
        SELECT AVG(s.avgLatencyMs) FROM Session s
        WHERE s.room.id = :roomId
        AND s.status = 'ACTIVE'
        AND s.avgLatencyMs IS NOT NULL
    """)
    Double calculateAverageLatency(@Param("roomId") UUID roomId);

    /**
     * Busca sesiones activas por versión de app
     *
     * @param appVersion versión de la aplicación
     * @return lista de sesiones con esa versión
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.appVersion = :appVersion
        AND s.status = 'ACTIVE'
    """)
    List<Session> findByAppVersion(@Param("appVersion") String appVersion);

    /**
     * Actualiza el estado de sesiones expiradas (batch update)
     *
     * @param threshold timestamp de umbral
     * @param status nuevo estado
     * @param disconnectReason razón de desconexión
     * @return número de registros actualizados
     */
    @Modifying
    @Query("""
        UPDATE Session s
        SET s.status = :status,
            s.endedAt = CURRENT_TIMESTAMP,
            s.disconnectReason = :disconnectReason
        WHERE s.lastHeartbeatAt < :threshold
        AND s.status = 'ACTIVE'
    """)
    int expireOldSessions(
        @Param("threshold") Instant threshold,
        @Param("status") SessionStatus status,
        @Param("disconnectReason") String disconnectReason
    );

    /**
     * Encuentra sesiones por rango de IP (seguridad)
     *
     * @param ipPattern patrón de IP (ej: "192.168.%")
     * @return lista de sesiones con IPs coincidentes
     */
    @Query(value = """
        SELECT * FROM dat_spd_session
        WHERE ip_address::text LIKE :ipPattern
        AND status = 'ACTIVE'
    """, nativeQuery = true)
    List<Session> findByIpPattern(@Param("ipPattern") String ipPattern);

    /**
     * Obtiene estadísticas de sesiones por sala
     *
     * @param roomId ID de la sala
     * @param since desde qué timestamp
     * @return estadísticas agregadas
     */
    @Query("""
        SELECT
            COUNT(s) as total,
            SUM(CASE WHEN s.status = 'ACTIVE' THEN 1 ELSE 0 END) as active,
            AVG(s.avgLatencyMs) as avgLatency,
            MAX(s.avgLatencyMs) as maxLatency
        FROM Session s
        WHERE s.room.id = :roomId
        AND s.startedAt >= :since
    """)
    Object[] getSessionStatsByRoom(
        @Param("roomId") UUID roomId,
        @Param("since") Instant since
    );
}
