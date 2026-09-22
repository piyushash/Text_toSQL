package com.piyush.aitexttosql.service;

import com.piyush.aitexttosql.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AiServiceTest {

    private ChatClient chatClient;
    private AiService aiService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        aiService = new AiService(chatClient);
    }

    @Test
    @DisplayName("generateResponse returns LLM content on success")
    void generateResponse_success() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Spring Boot is a Java framework for building production-ready applications.");

        String result = aiService.generateResponse("What is Spring Boot?");

        assertEquals("Spring Boot is a Java framework for building production-ready applications.", result);
        verify(chatClient).prompt();
        verify(requestSpec).user("What is Spring Boot?");
        verify(requestSpec).call();
        verify(callResponseSpec).content();
    }

    @Test
    @DisplayName("generateResponse trims whitespace from LLM response")
    void generateResponse_trimsWhitespace() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("  trimmed content  ");

        String result = aiService.generateResponse("test");

        assertEquals("trimmed content", result);
    }

    @Test
    @DisplayName("generateResponse rejects blank message")
    void generateResponse_blankMessage_throws() {
        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> aiService.generateResponse("")
        );
        assertTrue(ex.getMessage().contains("must not be null or blank"));
        verifyNoInteractions(chatClient);
    }

    @Test
    @DisplayName("generateResponse rejects null message")
    void generateResponse_nullMessage_throws() {
        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> aiService.generateResponse(null)
        );
        assertTrue(ex.getMessage().contains("must not be null or blank"));
        verifyNoInteractions(chatClient);
    }

    @Test
    @DisplayName("generateResponse wraps provider exception in AiServiceException")
    void generateResponse_providerFailure_throws() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenThrow(new RuntimeException("Provider connection failed"));

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> aiService.generateResponse("test")
        );
        assertEquals("AI service is temporarily unavailable", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("Provider connection failed", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("generateResponse throws when LLM returns null content")
    void generateResponse_nullContent_throws() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(null);

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> aiService.generateResponse("test")
        );
        assertEquals("AI service returned an empty response", ex.getMessage());
    }

    @Test
    @DisplayName("generateResponse throws when LLM returns blank content")
    void generateResponse_blankContent_throws() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("   ");

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> aiService.generateResponse("test")
        );
        assertEquals("AI service returned an empty response", ex.getMessage());
    }
}
