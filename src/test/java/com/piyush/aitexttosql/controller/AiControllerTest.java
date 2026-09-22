package com.piyush.aitexttosql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piyush.aitexttosql.exception.AiServiceException;
import com.piyush.aitexttosql.model.AiRequest;
import com.piyush.aitexttosql.service.AiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/ai/chat returns 200 with valid request")
    void chat_validRequest_returns200() throws Exception {
        String userMessage = "What is Spring Boot?";
        String aiResponseText = "Spring Boot is a Java framework.";

        when(aiService.generateResponse(eq(userMessage))).thenReturn(aiResponseText);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiRequest(userMessage))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(userMessage))
                .andExpect(jsonPath("$.response").value(aiResponseText));
    }

    @Test
    @DisplayName("POST /api/ai/chat returns 400 when message is blank")
    void chat_blankMessage_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("message must not be null or blank"));
    }

    @Test
    @DisplayName("POST /api/ai/chat returns 400 when message is missing")
    void chat_missingMessage_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/ai/chat returns 503 when AI service fails")
    void chat_aiServiceFailure_returns503() throws Exception {
        String userMessage = "Explain databases";
        when(aiService.generateResponse(eq(userMessage)))
                .thenThrow(new AiServiceException("AI service is temporarily unavailable"));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AiRequest(userMessage))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI service is temporarily unavailable"));
    }

    @Test
    @DisplayName("POST /api/ai/chat returns 400 when body is empty")
    void chat_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
