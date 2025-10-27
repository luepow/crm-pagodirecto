package com.pagodirecto.reportes.application.service.impl;

import com.pagodirecto.reportes.application.dto.ChatRequest;
import com.pagodirecto.reportes.application.dto.ChatResponse;
import com.pagodirecto.reportes.application.service.GeminiAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service Implementation: GeminiAIServiceImpl
 *
 * Integración con Google Gemini AI
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@Service
@Slf4j
public class GeminiAIServiceImpl implements GeminiAIService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.info("Enviando mensaje a Gemini AI: {}", request.getMessage());

        try {
            // Construir el request para Gemini
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();

            // Contexto del CRM
            String contextPrompt = "Eres un asistente de IA para un sistema CRM llamado PagoDirecto. " +
                    "Ayudas a los usuarios con información sobre clientes, oportunidades, tareas, productos y ventas. " +
                    "Responde de manera concisa y profesional en español. " +
                    "Si no tienes información específica, ofrece sugerencias generales útiles.\n\n" +
                    "Pregunta del usuario: " + request.getMessage();

            part.put("text", contextPrompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Request entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Llamada a Gemini API
            String url = geminiApiUrl + "?key=" + geminiApiKey;
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            // Extraer respuesta
            String aiResponse = extractResponse(response);

            String conversationId = request.getConversationId() != null
                    ? request.getConversationId()
                    : UUID.randomUUID().toString();

            log.info("Respuesta recibida de Gemini AI para conversación: {}", conversationId);

            return ChatResponse.builder()
                    .response(aiResponse)
                    .conversationId(conversationId)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Error al comunicarse con Gemini AI", e);

            // Respuesta de fallback
            return ChatResponse.builder()
                    .response("Lo siento, estoy experimentando dificultades técnicas en este momento. " +
                            "Por favor, intenta de nuevo más tarde o contacta al soporte técnico.")
                    .conversationId(request.getConversationId())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Error al extraer respuesta de Gemini", e);
        }
        return "No pude procesar tu solicitud correctamente.";
    }
}
