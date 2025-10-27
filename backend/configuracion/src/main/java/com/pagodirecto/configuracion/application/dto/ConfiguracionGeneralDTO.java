package com.pagodirecto.configuracion.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ConfiguracionGeneralDTO
 *
 * Configuración general del sistema
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Configuración general del sistema")
public class ConfiguracionGeneralDTO {

    @Schema(description = "Nombre de la empresa", example = "PagoDirecto S.A. de C.V.")
    private String nombreEmpresa;

    @Schema(description = "URL del logo de la empresa")
    private String logoUrl;

    @Schema(description = "Zona horaria", example = "America/Mexico_City")
    private String zonaHoraria;

    @Schema(description = "Moneda por defecto", example = "MXN")
    private String moneda;

    @Schema(description = "Idioma por defecto", example = "es-MX")
    private String idioma;

    @Schema(description = "Formato de fecha", example = "dd/MM/yyyy")
    private String formatoFecha;

    @Schema(description = "Formato de hora", example = "HH:mm:ss")
    private String formatoHora;

    @Schema(description = "Teléfono de contacto", example = "+52 55 1234 5678")
    private String telefonoContacto;

    @Schema(description = "Correo electrónico de contacto", example = "contacto@pagodirecto.com")
    private String emailContacto;

    @Schema(description = "Dirección física")
    private String direccion;
}
