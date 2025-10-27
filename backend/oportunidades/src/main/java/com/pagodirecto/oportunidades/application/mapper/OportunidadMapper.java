package com.pagodirecto.oportunidades.application.mapper;

import com.pagodirecto.oportunidades.application.dto.OportunidadDTO;
import com.pagodirecto.oportunidades.domain.Oportunidad;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper: Oportunidad
 *
 * MapStruct mapper para convertir entre entidades y DTOs del dominio Oportunidad.
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OportunidadMapper {

    /**
     * Convierte entidad Oportunidad a DTO
     *
     * @param oportunidad entidad
     * @return DTO
     */
    @Mapping(target = "valorPonderado", expression = "java(oportunidad.calcularValorPonderado())")
    OportunidadDTO toDTO(Oportunidad oportunidad);

    /**
     * Convierte DTO a entidad Oportunidad
     *
     * @param oportunidadDTO DTO
     * @return entidad
     */
    @Mapping(target = "actividades", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Oportunidad toEntity(OportunidadDTO oportunidadDTO);

    /**
     * Convierte lista de entidades a lista de DTOs
     *
     * @param oportunidades lista de entidades
     * @return lista de DTOs
     */
    List<OportunidadDTO> toDTOList(List<Oportunidad> oportunidades);

    /**
     * Actualiza una entidad existente con datos del DTO
     *
     * @param oportunidadDTO DTO con datos actualizados
     * @param oportunidad entidad a actualizar
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actividades", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(OportunidadDTO oportunidadDTO, @MappingTarget Oportunidad oportunidad);
}
