package com.pagodirecto.clientes.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Dirección
 *
 * Representa una dirección física asociada a un cliente.
 * Soporta direcciones fiscales, de envío y otros tipos con designación de dirección predeterminada.
 *
 * Tabla: clientes_direcciones
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "clientes_direcciones", indexes = {
    @Index(name = "idx_clientes_direcciones_cliente", columnList = "cliente_id"),
    @Index(name = "idx_clientes_direcciones_tipo", columnList = "tipo"),
    @Index(name = "idx_clientes_direcciones_default", columnList = "is_default")
})
@SQLDelete(sql = "UPDATE clientes_direcciones SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private DireccionTipo tipo;

    @Column(name = "calle", nullable = false, length = 255)
    private String calle;

    @Column(name = "numero_exterior", length = 20)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 20)
    private String numeroInterior;

    @Column(name = "colonia", length = 100)
    private String colonia;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "estado", nullable = false, length = 100)
    private String estado;

    @Column(name = "codigo_postal", nullable = false, length = 10)
    private String codigoPostal;

    @Column(name = "pais", nullable = false, length = 2)
    @Builder.Default
    private String pais = "MX";

    @Column(name = "referencia", columnDefinition = "TEXT")
    private String referencia;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

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
     * Construye la dirección completa formateada
     *
     * @return dirección completa como string
     */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(calle);
        if (numeroExterior != null) {
            sb.append(" #").append(numeroExterior);
        }
        if (numeroInterior != null) {
            sb.append(" Int. ").append(numeroInterior);
        }
        if (colonia != null) {
            sb.append(", Col. ").append(colonia);
        }
        sb.append(", ").append(ciudad).append(", ").append(estado);
        sb.append(" C.P. ").append(codigoPostal);
        sb.append(", ").append(pais);
        return sb.toString();
    }

    /**
     * Establece esta dirección como predeterminada para su tipo
     * Nota: La lógica para desmarcar otras direcciones debe manejarse en el servicio
     */
    public void establecerComoPredeterminada() {
        this.isDefault = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Remueve la designación de dirección predeterminada
     */
    public void removerComoPredeterminada() {
        this.isDefault = false;
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
