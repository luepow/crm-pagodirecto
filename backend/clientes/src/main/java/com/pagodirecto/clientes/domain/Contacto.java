package com.pagodirecto.clientes.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Contacto
 *
 * Representa una persona de contacto asociada a un cliente.
 * Permite gestionar múltiples contactos por cliente con designación de contacto principal.
 *
 * Tabla: clientes_contactos
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "clientes_contactos", indexes = {
    @Index(name = "idx_clientes_contactos_cliente", columnList = "cliente_id"),
    @Index(name = "idx_clientes_contactos_email", columnList = "email"),
    @Index(name = "idx_clientes_contactos_primary", columnList = "is_primary")
})
@SQLDelete(sql = "UPDATE clientes_contactos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "telefono_movil", length = 50)
    private String telefonoMovil;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Establece este contacto como principal
     * Nota: La lógica para desmarcar otros contactos debe manejarse en el servicio
     */
    public void establecerComoPrincipal() {
        this.isPrimary = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Remueve la designación de contacto principal
     */
    public void removerComoPrincipal() {
        this.isPrimary = false;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
