package com.piyush.aitexttosql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piyush.aitexttosql.exception.AiServiceException;
import com.piyush.aitexttosql.exception.InvalidQuestionException;
import com.piyush.aitexttosql.model.TextToSqlRequest;
import com.piyush.aitexttosql.service.TextToSqlService;
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

@WebMvcTest(TextToSqlController.class)
class TextToSqlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TextToSqlService textToSqlService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/text-to-sql returns 200 with generated SQL on valid question")
    void generateSql_validQuestion_returns200() throws Exception {
        String question = "Show all customers from Delhi";
        String generatedSql = "SELECT * FROM customers WHERE city = 'Delhi';";

        when(textToSqlService.generateSql(eq(question))).thenReturn(generatedSql);

        mockMvc.perform(post("/api/text-to-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextToSqlRequest(question))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value(question))
                .andExpect(jsonPath("$.sql").value(generatedSql))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/text-to-sql returns 400 when question is blank")
    void generateSql_blankQuestion_returns400() throws Exception {
        mockMvc.perform(post("/api/text-to-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("question must not be null or blank"));
    }

    @Test
    @DisplayName("POST /api/text-to-sql returns 400 when question field is missing")
    void generateSql_missingQuestion_returns400() throws Exception {
        mockMvc.perform(post("/api/text-to-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/text-to-sql returns 400 with custom response when question cannot be answered from schema")
    void generateSql_invalidQuestion_returns400WithDetails() throws Exception {
        String question = "Show me employee salary information";
        when(textToSqlService.generateSql(eq(question)))
                .thenThrow(new InvalidQuestionException(question, "The question cannot be answered using the available database schema."));

        mockMvc.perform(post("/api/text-to-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextToSqlRequest(question))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.question").value(question))
                .andExpect(jsonPath("$.sql").doesNotExist())
                .andExpect(jsonPath("$.error").value("The question cannot be answered using the available database schema."));
    }

    @Test
    @DisplayName("POST /api/text-to-sql returns 503 when AI service fails")
    void generateSql_aiServiceFailure_returns503() throws Exception {
        String question = "Show all orders";
        when(textToSqlService.generateSql(eq(question)))
                .thenThrow(new AiServiceException("AI service is temporarily unavailable"));

        mockMvc.perform(post("/api/text-to-sql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TextToSqlRequest(question))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI service is temporarily unavailable"));
    }
}
