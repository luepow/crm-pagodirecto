package com.empresa.crm.seguridad.repository;

import com.empresa.crm.seguridad.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameAndDeletedAtIsNull(String username);

    Optional<Usuario> findByEmailAndDeletedAtIsNull(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<Usuario> findByUsernameWithRoles(String username);

    Boolean existsByUsernameAndDeletedAtIsNull(String username);

    Boolean existsByEmailAndDeletedAtIsNull(String email);
}
