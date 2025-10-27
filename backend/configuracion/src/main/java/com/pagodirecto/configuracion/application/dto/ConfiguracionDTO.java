package com.pagodirecto.configuracion.application.dto;

import com.pagodirecto.configuracion.domain.ConfiguracionCategoria;
import com.pagodirecto.configuracion.domain.ConfiguracionTipoDato;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO: ConfiguracionDTO
 *
 * Representación de una configuración del sistema
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Configuración del sistema")
public class ConfiguracionDTO {

    @Schema(description = "ID único de la configuración")
    private UUID id;

    @Schema(description = "ID de la unidad de negocio (null para configuraciones globales)")
    private UUID unidadNegocioId;

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 100, message = "La clave no puede exceder 100 caracteres")
    @Schema(description = "Clave única de la configuración", example = "app.nombre")
    private String clave;

    @Schema(description = "Valor actual de la configuración", example = "PagoDirecto CRM")
    private String valor;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "Categoría de la configuración", example = "GENERAL")
    private ConfiguracionCategoria categoria;

    @NotNull(message = "El tipo de dato es obligatorio")
    @Schema(description = "Tipo de dato del valor", example = "STRING")
    private ConfiguracionTipoDato tipoDato;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre descriptivo", example = "Nombre de la aplicación")
    private String nombre;

    @Schema(description = "Descripción de la configuración")
    private String descripcion;

    @Schema(description = "Valor por defecto")
    private String valorPorDefecto;

    @Schema(description = "Indica si la configuración es pública", example = "true")
    private Boolean esPublica;

    @Schema(description = "Indica si la configuración es modificable", example = "true")
    private Boolean esModificable;

    @Schema(description = "Expresión regular para validación del valor")
    private String validacionRegex;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;
}
