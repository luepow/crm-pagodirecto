package com.pagodirecto.spidi.infrastructure.repository;

import com.pagodirecto.spidi.domain.Room;
import com.pagodirecto.spidi.domain.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Room
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * Busca una sala por código (sin soft-deleted)
     *
     * @param code código de la sala
     * @return Optional con la sala si existe
     */
    Optional<Room> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Busca salas por estado
     *
     * @param status estado de la sala
     * @return lista de salas con el estado especificado
     */
    List<Room> findByStatusAndDeletedAtIsNull(RoomStatus status);

    /**
     * Busca salas activas de una unidad de negocio
     *
     * @param unidadNegocioId ID de la unidad de negocio
     * @param status estado de la sala
     * @return lista de salas activas
     */
    @Query("""
        SELECT r FROM Room r
        WHERE r.unidadNegocioId = :unidadNegocioId
        AND r.deletedAt IS NULL
        AND r.status = :status
        ORDER BY r.name ASC
    """)
    List<Room> findActiveRoomsByUnidadNegocio(
        @Param("unidadNegocioId") UUID unidadNegocioId,
        @Param("status") RoomStatus status
    );

    /**
     * Cuenta sesiones activas en una sala
     *
     * @param roomId ID de la sala
     * @return número de sesiones activas
     */
    @Query("""
        SELECT COUNT(s) FROM Session s
        WHERE s.room.id = :roomId
        AND s.status = 'ACTIVE'
    """)
    Long countActiveSessions(@Param("roomId") UUID roomId);

    /**
     * Busca salas por tipo
     *
     * @param roomTypeId ID del tipo de sala
     * @return lista de salas del tipo especificado
     */
    @Query("""
        SELECT r FROM Room r
        WHERE r.roomType.id = :roomTypeId
        AND r.deletedAt IS NULL
        ORDER BY r.name ASC
    """)
    List<Room> findByRoomType(@Param("roomTypeId") UUID roomTypeId);

    /**
     * Busca salas por tags (JSONB query)
     *
     * @param tag tag a buscar
     * @return lista de salas que contienen el tag
     */
    @Query(value = """
        SELECT * FROM dat_spd_room
        WHERE deleted_at IS NULL
        AND tags::jsonb @> :tag::jsonb
    """, nativeQuery = true)
    List<Room> findByTag(@Param("tag") String tag);

    /**
     * Obtiene el resumen de salas con métricas
     *
     * @param unidadNegocioId ID de la unidad de negocio
     * @return lista de salas con métricas
     */
    @Query("""
        SELECT r, COUNT(s.id) as sessionCount
        FROM Room r
        LEFT JOIN Session s ON s.room.id = r.id AND s.status = 'ACTIVE'
        WHERE r.unidadNegocioId = :unidadNegocioId
        AND r.deletedAt IS NULL
        GROUP BY r
        ORDER BY sessionCount DESC
    """)
    List<Object[]> findRoomSummaryWithMetrics(@Param("unidadNegocioId") UUID unidadNegocioId);
}
