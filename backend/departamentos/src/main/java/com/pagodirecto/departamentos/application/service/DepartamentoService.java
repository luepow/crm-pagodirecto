package com.pagodirecto.departamentos.application.service;

import com.pagodirecto.departamentos.application.dto.CreateDepartamentoRequest;
import com.pagodirecto.departamentos.application.dto.DepartamentoDTO;
import com.pagodirecto.departamentos.application.dto.UpdateDepartamentoRequest;

import java.util.List;
import java.util.UUID;

/**
 * Servicio: DepartamentoService
 *
 * Servicio de gestión CRUD de departamentos
 *
 * @author PagoDirecto Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface DepartamentoService {

    /**
     * Obtiene todos los departamentos
     */
    List<DepartamentoDTO> getAllDepartamentos();

    /**
     * Obtiene departamentos por unidad de negocio
     */
    List<DepartamentoDTO> getDepartamentosByUnidadNegocio(UUID unidadNegocioId);

    /**
     * Obtiene departamentos activos por unidad de negocio
     */
    List<DepartamentoDTO> getDepartamentosActivosByUnidadNegocio(UUID unidadNegocioId);

    /**
     * Obtiene departamentos raíz (sin padre)
     */
    List<DepartamentoDTO> getDepartamentosRaiz();

    /**
     * Obtiene sub-departamentos de un departamento
     */
    List<DepartamentoDTO> getSubDepartamentos(UUID parentId);

    /**
     * Obtiene un departamento por su ID
     */
    DepartamentoDTO getDepartamentoById(UUID id);

    /**
     * Obtiene un departamento por su código
     */
    DepartamentoDTO getDepartamentoByCodigo(String codigo);

    /**
     * Crea un nuevo departamento
     */
    DepartamentoDTO createDepartamento(CreateDepartamentoRequest request, UUID creatorId);

    /**
     * Actualiza un departamento existente
     */
    DepartamentoDTO updateDepartamento(UUID id, UpdateDepartamentoRequest request, UUID updaterId);

    /**
     * Elimina un departamento (soft delete)
     */
    void deleteDepartamento(UUID id);

    /**
     * Activa/desactiva un departamento
     */
    void toggleActivoDepartamento(UUID id);
}
