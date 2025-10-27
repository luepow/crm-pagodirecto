package com.pagodirecto.tareas.application.mapper;

import com.pagodirecto.tareas.application.dto.TareaDTO;
import com.pagodirecto.tareas.domain.Tarea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper: Tarea <-> TareaDTO
 *
 * Mapper MapStruct para conversión entre entidad Tarea y DTO.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Mapper(componentModel = "spring")
public interface TareaMapper {

    /**
     * Convierte entidad Tarea a DTO
     * Los campos asignadoNombre, createdByNombre, relacionadoNombre y vencida
     * deben ser establecidos manualmente en el servicio
     */
    @Mapping(target = "asignadoNombre", ignore = true)
    @Mapping(target = "createdByNombre", ignore = true)
    @Mapping(target = "relacionadoNombre", ignore = true)
    @Mapping(target = "vencida", expression = "java(tarea.isVencida())")
    TareaDTO toDTO(Tarea tarea);

    /**
     * Convierte DTO a entidad Tarea
     */
    @Mapping(target = "comentarios", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Tarea toEntity(TareaDTO tareaDTO);

    /**
     * Actualiza una entidad existente con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comentarios", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(TareaDTO tareaDTO, @MappingTarget Tarea tarea);
}
