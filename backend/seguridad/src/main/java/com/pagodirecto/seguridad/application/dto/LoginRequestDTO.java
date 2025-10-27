package com.pagodirecto.seguridad.application.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitud de login
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
