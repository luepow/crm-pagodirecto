package com.pagodirecto.reportes.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO: ChatRequest
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    private String conversationId;
}
