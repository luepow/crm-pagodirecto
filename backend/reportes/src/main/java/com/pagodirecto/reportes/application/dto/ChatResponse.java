package com.pagodirecto.reportes.application.dto;

import lombok.*;

/**
 * DTO: ChatResponse
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String response;
    private String conversationId;
    private Long timestamp;
}
