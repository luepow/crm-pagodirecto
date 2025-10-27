package com.pagodirecto.clientes.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Cliente
 *
 * Representa un cliente (persona física o moral) en el sistema CRM.
 * Incluye información básica de contacto, clasificación y relaciones con contactos y direcciones.
 *
 * Tabla: clientes_clientes
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "clientes_clientes", indexes = {
    @Index(name = "idx_clientes_clientes_codigo", columnList = "codigo"),
    @Index(name = "idx_clientes_clientes_email", columnList = "email"),
    @Index(name = "idx_clientes_clientes_rfc", columnList = "rfc"),
    @Index(name = "idx_clientes_clientes_unidad_negocio", columnList = "unidad_negocio_id"),
    @Index(name = "idx_clientes_clientes_status", columnList = "status"),
    @Index(name = "idx_clientes_clientes_propietario", columnList = "propietario_id")
})
@SQLDelete(sql = "UPDATE clientes_clientes SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "codigo", nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @Builder.Default
    private ClienteTipo tipo = ClienteTipo.PERSONA;

    @Column(name = "rfc", length = 20)
    private String rfc;

    @Column(name = "razon_social", length = 255)
    private String razonSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ClienteStatus status = ClienteStatus.ACTIVE;

    @Column(name = "segmento", length = 50)
    private String segmento;

    @Column(name = "fuente", length = 50)
    private String fuente;

    @Column(name = "propietario_id")
    private UUID propietarioId;

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

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Contacto> contactos = new HashSet<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Direccion> direcciones = new HashSet<>();

    /**
     * Verifica si el cliente está activo
     *
     * @return true si el status es ACTIVE, false en caso contrario
     */
    public boolean isActive() {
        return ClienteStatus.ACTIVE.equals(this.status);
    }

    /**
     * Verifica si el cliente es empresa
     *
     * @return true si el tipo es EMPRESA, false si es PERSONA
     */
    public boolean isEmpresa() {
        return ClienteTipo.EMPRESA.equals(this.tipo);
    }

    /**
     * Activa el cliente
     */
    public void activar() {
        this.status = ClienteStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Desactiva el cliente
     */
    public void desactivar() {
        this.status = ClienteStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Convierte un lead a prospecto
     */
    public void convertirAProspecto() {
        if (ClienteStatus.LEAD.equals(this.status)) {
            this.status = ClienteStatus.PROSPECT;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Convierte un prospecto a cliente activo
     */
    public void convertirACliente() {
        if (ClienteStatus.PROSPECT.equals(this.status)) {
            this.status = ClienteStatus.ACTIVE;
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Agrega el cliente a la lista negra
     *
     * @param motivo motivo del blacklist
     */
    public void agregarABlacklist(String motivo) {
        this.status = ClienteStatus.BLACKLIST;
        this.notas = (this.notas != null ? this.notas + "\n" : "") +
                    "BLACKLIST: " + motivo + " - " + Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Agrega un contacto al cliente
     *
     * @param contacto el contacto a agregar
     */
    public void agregarContacto(Contacto contacto) {
        contacto.setCliente(this);
        this.contactos.add(contacto);
        this.updatedAt = Instant.now();
    }

    /**
     * Agrega una dirección al cliente
     *
     * @param direccion la dirección a agregar
     */
    public void agregarDireccion(Direccion direccion) {
        direccion.setCliente(this);
        this.direcciones.add(direccion);
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
