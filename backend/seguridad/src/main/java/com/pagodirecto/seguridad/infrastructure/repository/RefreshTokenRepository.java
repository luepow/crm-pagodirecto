package com.pagodirecto.seguridad.infrastructure.repository;

import com.pagodirecto.seguridad.domain.RefreshToken;
import com.pagodirecto.seguridad.domain.Usuario;
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
 * Repositorio: RefreshTokenRepository
 *
 * Repositorio de acceso a datos para la entidad RefreshToken
 *
 * @author PagoDirecto Security Team
 * @version 1.0
 * @since 2025-10-13
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Busca un refresh token por su hash
     *
     * @param tokenHash el hash del token
     * @return Optional con el token si existe
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Busca todos los tokens de un usuario
     *
     * @param usuario el usuario
     * @return lista de tokens
     */
    List<RefreshToken> findByUsuario(Usuario usuario);

    /**
     * Busca todos los tokens válidos (no revocados y no expirados) de un usuario
     *
     * @param usuario el usuario
     * @param now     timestamp actual
     * @return lista de tokens válidos
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.usuario = :usuario " +
           "AND rt.revocado = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUsuario(@Param("usuario") Usuario usuario, @Param("now") Instant now);

    /**
     * Revoca todos los tokens de un usuario
     *
     * @param usuario el usuario
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true, rt.revocadoAt = :now " +
           "WHERE rt.usuario = :usuario AND rt.revocado = false")
    void revokeAllTokensByUsuario(@Param("usuario") Usuario usuario, @Param("now") Instant now);

    /**
     * Elimina todos los tokens expirados
     *
     * @param now timestamp actual
     * @return número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Elimina todos los tokens revocados con más de 30 días de antigüedad
     *
     * @param threshold fecha límite
     * @return número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revocado = true AND rt.revocadoAt < :threshold")
    int deleteOldRevokedTokens(@Param("threshold") Instant threshold);

    /**
     * Cuenta los tokens válidos de un usuario
     *
     * @param usuario el usuario
     * @param now     timestamp actual
     * @return número de tokens válidos
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.usuario = :usuario " +
           "AND rt.revocado = false AND rt.expiresAt > :now")
    long countValidTokensByUsuario(@Param("usuario") Usuario usuario, @Param("now") Instant now);
}
