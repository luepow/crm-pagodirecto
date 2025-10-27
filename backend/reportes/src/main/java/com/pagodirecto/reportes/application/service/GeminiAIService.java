package com.pagodirecto.reportes.application.service;

import com.pagodirecto.reportes.application.dto.ChatRequest;
import com.pagodirecto.reportes.application.dto.ChatResponse;

/**
 * Service: GeminiAIService
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
public interface GeminiAIService {

    ChatResponse chat(ChatRequest request);
}
