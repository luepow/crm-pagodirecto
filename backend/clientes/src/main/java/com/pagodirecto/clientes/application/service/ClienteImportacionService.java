package com.pagodirecto.clientes.application.service;

import com.pagodirecto.clientes.application.dto.ImportacionResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Servicio: Importación de Clientes
 *
 * Interface de servicio para operaciones de importación masiva de clientes desde archivos.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface ClienteImportacionService {

    /**
     * Importa clientes desde un archivo CSV
     *
     * CSV esperado: una columna con nombres de empresas (sin encabezado requerido)
     * Ejemplo:
     * Empresa ABC, C.A.
     * Comercial XYZ
     * Distribuidora 123, S.A.
     *
     * @param file archivo CSV con los datos
     * @param unidadNegocioId UUID de la unidad de negocio
     * @param usuarioId UUID del usuario que realiza la importación
     * @return resultado de la importación con estadísticas
     */
    ImportacionResultDTO importarDesdeCSV(MultipartFile file, UUID unidadNegocioId, UUID usuarioId);
}
