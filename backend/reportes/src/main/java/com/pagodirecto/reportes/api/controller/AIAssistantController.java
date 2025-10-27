package com.pagodirecto.reportes.api.controller;

import com.pagodirecto.reportes.application.dto.ChatRequest;
import com.pagodirecto.reportes.application.dto.ChatResponse;
import com.pagodirecto.reportes.application.service.GeminiAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller: AI Assistant
 *
 * @author PagoDirecto CRM Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Assistant", description = "API del asistente de IA con Gemini")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AIAssistantController {

    private final GeminiAIService geminiAIService;

    @PostMapping("/chat")
    @Operation(summary = "Enviar mensaje al asistente de IA")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : "anonymous";
        log.info("Usuario {} enviando mensaje al asistente IA", username);

        ChatResponse response = geminiAIService.chat(request);
        return ResponseEntity.ok(response);
    }
}
