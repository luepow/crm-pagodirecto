package com.pagodirecto.configuracion.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio: Configuracion
 *
 * Representa una configuración del sistema con patrón key-value
 * Soporta múltiples tipos de datos y categorización
 *
 * Tabla: configuracion_settings
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Entity
@Table(name = "configuracion_settings", indexes = {
    @Index(name = "idx_configuracion_clave", columnList = "clave", unique = true),
    @Index(name = "idx_configuracion_categoria", columnList = "categoria"),
    @Index(name = "idx_configuracion_unidad_negocio", columnList = "unidad_negocio_id")
})
@SQLDelete(sql = "UPDATE configuracion_settings SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "unidad_negocio_id")
    private UUID unidadNegocioId;

    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 30)
    private ConfiguracionCategoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 20)
    @Builder.Default
    private ConfiguracionTipoDato tipoDato = ConfiguracionTipoDato.STRING;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "valor_por_defecto", columnDefinition = "TEXT")
    private String valorPorDefecto;

    @Column(name = "es_publica", nullable = false)
    @Builder.Default
    private Boolean esPublica = false;

    @Column(name = "es_modificable", nullable = false)
    @Builder.Default
    private Boolean esModificable = true;

    @Column(name = "validacion_regex", length = 255)
    private String validacionRegex;

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
     * Valida que el valor cumpla con el tipo de dato especificado
     *
     * @param valor valor a validar
     * @return true si el valor es válido, false en caso contrario
     */
    public boolean validarValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }

        try {
            switch (tipoDato) {
                case INTEGER:
                    Integer.parseInt(valor);
                    break;
                case BOOLEAN:
                    if (!valor.equalsIgnoreCase("true") && !valor.equalsIgnoreCase("false")) {
                        return false;
                    }
                    break;
                case DECIMAL:
                    Double.parseDouble(valor);
                    break;
                case EMAIL:
                    if (!valor.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        return false;
                    }
                    break;
                case URL:
                    if (!valor.matches("^(http|https)://.*$")) {
                        return false;
                    }
                    break;
                case PHONE:
                    if (!valor.matches("^[+]?[0-9\\s-()]{7,20}$")) {
                        return false;
                    }
                    break;
                // STRING y JSON no requieren validación adicional
            }

            // Validación adicional con regex personalizado
            if (validacionRegex != null && !validacionRegex.isBlank()) {
                return valor.matches(validacionRegex);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el valor como String (raw)
     */
    public String getValorString() {
        return valor != null ? valor : valorPorDefecto;
    }

    /**
     * Obtiene el valor como Integer
     */
    public Integer getValorInteger() {
        String val = getValorString();
        return val != null ? Integer.parseInt(val) : null;
    }

    /**
     * Obtiene el valor como Boolean
     */
    public Boolean getValorBoolean() {
        String val = getValorString();
        return val != null ? Boolean.parseBoolean(val) : null;
    }

    /**
     * Obtiene el valor como Double
     */
    public Double getValorDouble() {
        String val = getValorString();
        return val != null ? Double.parseDouble(val) : null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
