package com.pagodirecto.clientes.application.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO: Resultado de Importación
 *
 * Contiene las estadísticas y errores de un proceso de importación de clientes.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacionResultDTO {

    private int totalRegistros;
    private int registrosExitosos;
    private int registrosConErrores;

    @Builder.Default
    private List<String> errores = new ArrayList<>();

    @Builder.Default
    private List<ClienteDTO> clientesCreados = new ArrayList<>();

    private String mensaje;

    /**
     * Calcula la tasa de éxito de la importación
     *
     * @return porcentaje de registros exitosos
     */
    public double getTasaExito() {
        if (totalRegistros == 0) {
            return 0.0;
        }
        return (registrosExitosos * 100.0) / totalRegistros;
    }

    /**
     * Verifica si la importación fue completamente exitosa
     *
     * @return true si todos los registros se importaron correctamente
     */
    public boolean isExitoCompleto() {
        return registrosConErrores == 0 && registrosExitosos > 0;
    }

    /**
     * Agrega un error al resultado
     *
     * @param linea número de línea con error
     * @param mensaje mensaje de error
     */
    public void agregarError(int linea, String mensaje) {
        this.errores.add("Línea " + linea + ": " + mensaje);
        this.registrosConErrores++;
    }

    /**
     * Agrega un cliente creado al resultado
     *
     * @param cliente cliente creado
     */
    public void agregarClienteCreado(ClienteDTO cliente) {
        this.clientesCreados.add(cliente);
        this.registrosExitosos++;
    }
}
