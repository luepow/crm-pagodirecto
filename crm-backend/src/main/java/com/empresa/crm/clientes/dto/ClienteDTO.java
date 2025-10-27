package com.empresa.crm.clientes.dto;

import com.empresa.crm.clientes.model.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String pais;
    private String codigoPostal;
    private String rfc;
    private Boolean activo;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    /**
     * Convert Cliente entity to DTO
     */
    public static ClienteDTO fromEntity(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .ciudad(cliente.getCiudad())
                .pais(cliente.getPais())
                .codigoPostal(cliente.getCodigoPostal())
                .rfc(cliente.getRfc())
                .activo(cliente.getActivo())
                .createdAt(cliente.getCreatedAt())
                .createdBy(cliente.getCreatedBy())
                .updatedAt(cliente.getUpdatedAt())
                .updatedBy(cliente.getUpdatedBy())
                .build();
    }
}
