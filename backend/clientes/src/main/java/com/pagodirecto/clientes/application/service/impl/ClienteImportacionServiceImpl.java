package com.pagodirecto.clientes.application.service.impl;

import com.pagodirecto.clientes.application.dto.ClienteDTO;
import com.pagodirecto.clientes.application.dto.ImportacionResultDTO;
import com.pagodirecto.clientes.application.mapper.ClienteMapper;
import com.pagodirecto.clientes.application.service.ClienteImportacionService;
import com.pagodirecto.clientes.domain.Cliente;
import com.pagodirecto.clientes.domain.ClienteStatus;
import com.pagodirecto.clientes.domain.ClienteTipo;
import com.pagodirecto.clientes.infrastructure.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementación: ClienteImportacionService
 *
 * Servicio para importación masiva de clientes desde archivos CSV.
 * Genera códigos únicos automáticamente y aplica configuraciones para empresas venezolanas.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteImportacionServiceImpl implements ClienteImportacionService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ImportacionResultDTO importarDesdeCSV(MultipartFile file, UUID unidadNegocioId, UUID usuarioId) {
        log.info("Iniciando importación de clientes desde CSV - usuario: {}", usuarioId);

        ImportacionResultDTO resultado = ImportacionResultDTO.builder()
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosConErrores(0)
                .build();

        if (file.isEmpty()) {
            resultado.setMensaje("El archivo está vacío");
            return resultado;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            int numeroLinea = 0;

            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                resultado.setTotalRegistros(numeroLinea);

                // Ignorar líneas vacías
                if (linea.trim().isEmpty()) {
                    continue;
                }

                // Procesar la línea como nombre de empresa
                try {
                    String nombreEmpresa = linea.trim();

                    // Validar que el nombre no esté duplicado
                    if (clienteRepository.existsByNombreAndUnidadNegocioId(nombreEmpresa, unidadNegocioId)) {
                        resultado.agregarError(numeroLinea, "Cliente duplicado: " + nombreEmpresa);
                        continue;
                    }

                    // Generar código único
                    String codigo = generarCodigoUnico(nombreEmpresa);

                    // Crear el cliente
                    Cliente cliente = Cliente.builder()
                            .unidadNegocioId(unidadNegocioId)
                            .codigo(codigo)
                            .nombre(nombreEmpresa)
                            .razonSocial(nombreEmpresa)
                            .tipo(ClienteTipo.EMPRESA)
                            .status(ClienteStatus.ACTIVE)
                            .fuente("IMPORTACION_CSV")
                            .createdBy(usuarioId)
                            .updatedBy(usuarioId)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();

                    cliente = clienteRepository.save(cliente);

                    ClienteDTO clienteDTO = clienteMapper.toDTO(cliente);
                    resultado.agregarClienteCreado(clienteDTO);

                    log.debug("Cliente creado exitosamente: {} (código: {})", nombreEmpresa, codigo);

                } catch (Exception e) {
                    log.error("Error procesando línea {}: {}", numeroLinea, e.getMessage(), e);
                    resultado.agregarError(numeroLinea, "Error: " + e.getMessage());
                }
            }

            // Generar mensaje de resumen
            if (resultado.isExitoCompleto()) {
                resultado.setMensaje(String.format(
                        "Importación exitosa: %d clientes creados",
                        resultado.getRegistrosExitosos()
                ));
            } else if (resultado.getRegistrosExitosos() > 0) {
                resultado.setMensaje(String.format(
                        "Importación parcial: %d exitosos, %d con errores",
                        resultado.getRegistrosExitosos(),
                        resultado.getRegistrosConErrores()
                ));
            } else {
                resultado.setMensaje("Importación fallida: no se pudo crear ningún cliente");
            }

            log.info("Importación completada - Total: {}, Exitosos: {}, Errores: {}",
                    resultado.getTotalRegistros(),
                    resultado.getRegistrosExitosos(),
                    resultado.getRegistrosConErrores());

        } catch (Exception e) {
            log.error("Error leyendo archivo CSV: {}", e.getMessage(), e);
            resultado.setMensaje("Error leyendo el archivo: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Genera un código único para el cliente basado en el nombre
     * Formato: CLI-{PREFIJO}-{TIMESTAMP}
     *
     * @param nombreEmpresa nombre de la empresa
     * @return código único generado
     */
    private String generarCodigoUnico(String nombreEmpresa) {
        // Extraer primeras 3 letras del nombre (sin espacios ni caracteres especiales)
        String prefijo = nombreEmpresa.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (prefijo.length() > 3) {
            prefijo = prefijo.substring(0, 3);
        } else if (prefijo.length() < 3) {
            prefijo = String.format("%-3s", prefijo).replace(' ', 'X');
        }

        // Agregar timestamp para garantizar unicidad
        long timestamp = System.currentTimeMillis() % 100000; // Últimos 5 dígitos

        String codigo = String.format("CLI-%s-%05d", prefijo, timestamp);

        // Verificar que no exista (muy improbable pero por seguridad)
        int intentos = 0;
        while (clienteRepository.existsByCodigo(codigo) && intentos < 10) {
            timestamp = (timestamp + 1) % 100000;
            codigo = String.format("CLI-%s-%05d", prefijo, timestamp);
            intentos++;
        }

        return codigo;
    }
}
