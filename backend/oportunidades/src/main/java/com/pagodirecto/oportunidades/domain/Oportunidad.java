package com.pagodirecto.oportunidades.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de dominio: Oportunidad
 *
 * Representa una oportunidad de venta en el pipeline.
 *
 * Tabla: oportunidades_oportunidades
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "oportunidades_oportunidades", indexes = {
    @Index(name = "idx_oportunidades_oportunidades_cliente", columnList = "cliente_id"),
    @Index(name = "idx_oportunidades_oportunidades_etapa", columnList = "etapa_id"),
    @Index(name = "idx_oportunidades_oportunidades_propietario", columnList = "propietario_id"),
    @Index(name = "idx_oportunidades_oportunidades_unidad_negocio", columnList = "unidad_negocio_id")
})
@SQLDelete(sql = "UPDATE oportunidades_oportunidades SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oportunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "unidad_negocio_id", nullable = false)
    private UUID unidadNegocioId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "valor_estimado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "MXN";

    @Column(name = "probabilidad", nullable = false, precision = 5, scale = 2)
    private BigDecimal probabilidad;

    @Column(name = "etapa_id", nullable = false)
    private UUID etapaId;

    @Column(name = "fecha_cierre_estimada")
    private LocalDate fechaCierreEstimada;

    @Column(name = "fecha_cierre_real")
    private LocalDate fechaCierreReal;

    @Column(name = "propietario_id", nullable = false)
    private UUID propietarioId;

    @Column(name = "fuente", length = 50)
    private String fuente;

    @Column(name = "motivo_perdida", columnDefinition = "TEXT")
    private String motivoPerdida;

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

    @OneToMany(mappedBy = "oportunidad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ActividadOportunidad> actividades = new HashSet<>();

    /**
     * Calcula el valor ponderado de la oportunidad
     *
     * @return valor estimado * probabilidad / 100
     */
    public BigDecimal calcularValorPonderado() {
        return valorEstimado.multiply(probabilidad).divide(BigDecimal.valueOf(100));
    }

    /**
     * Mueve la oportunidad a una nueva etapa
     *
     * @param nuevaEtapaId UUID de la nueva etapa
     * @param nuevaProbabilidad nueva probabilidad de cierre
     */
    public void moverAEtapa(UUID nuevaEtapaId, BigDecimal nuevaProbabilidad) {
        this.etapaId = nuevaEtapaId;
        this.probabilidad = nuevaProbabilidad;
        this.updatedAt = Instant.now();
    }

    /**
     * Marca la oportunidad como ganada
     *
     * @param fechaCierre fecha de cierre real
     */
    public void marcarComoGanada(LocalDate fechaCierre) {
        this.fechaCierreReal = fechaCierre;
        this.probabilidad = BigDecimal.valueOf(100);
        this.updatedAt = Instant.now();
    }

    /**
     * Marca la oportunidad como perdida
     *
     * @param motivo motivo de la pérdida
     */
    public void marcarComoPerdida(String motivo) {
        this.motivoPerdida = motivo;
        this.probabilidad = BigDecimal.ZERO;
        this.fechaCierreReal = LocalDate.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Agrega una actividad a la oportunidad
     *
     * @param actividad actividad a agregar
     */
    public void agregarActividad(ActividadOportunidad actividad) {
        actividad.setOportunidad(this);
        this.actividades.add(actividad);
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
