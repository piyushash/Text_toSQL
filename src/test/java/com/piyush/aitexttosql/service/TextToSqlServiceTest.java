package com.piyush.aitexttosql.service;

import com.piyush.aitexttosql.exception.AiServiceException;
import com.piyush.aitexttosql.exception.InvalidQuestionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TextToSqlServiceTest {

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;
    private TextToSqlService textToSqlService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        textToSqlService = new TextToSqlService(chatClient);
    }

    @Test
    @DisplayName("Test 1: 'Show all customers from Delhi' generates query with SELECT, customers, city, Delhi")
    void generateSql_customersFromDelhi() {
        String expectedSql = "SELECT * FROM customers WHERE city = 'Delhi';";
        when(callResponseSpec.content()).thenReturn(expectedSql);

        String result = textToSqlService.generateSql("Show all customers from Delhi");

        assertNotNull(result);
        assertTrue(result.contains("SELECT"));
        assertTrue(result.contains("customers"));
        assertTrue(result.contains("city"));
        assertTrue(result.contains("Delhi"));
        assertEquals(expectedSql, result);

        verify(requestSpec).system(anyString());
        verify(requestSpec).user("Show all customers from Delhi");
    }

    @Test
    @DisplayName("Test 2: 'How many customers are there?' generates query with COUNT, customers")
    void generateSql_countCustomers() {
        String expectedSql = "SELECT COUNT(*) FROM customers;";
        when(callResponseSpec.content()).thenReturn(expectedSql);

        String result = textToSqlService.generateSql("How many customers are there?");

        assertNotNull(result);
        assertTrue(result.contains("COUNT"));
        assertTrue(result.contains("customers"));
        assertEquals(expectedSql, result);
    }

    @Test
    @DisplayName("Test 3: 'Show customer names and order amounts' generates query with customers, orders, JOIN")
    void generateSql_customerOrdersJoin() {
        String expectedSql = "SELECT c.name, o.amount FROM customers c JOIN orders o ON c.id = o.customer_id;";
        when(callResponseSpec.content()).thenReturn(expectedSql);

        String result = textToSqlService.generateSql("Show customer names and order amounts");

        assertNotNull(result);
        assertTrue(result.contains("customers"));
        assertTrue(result.contains("orders"));
        assertTrue(result.contains("JOIN"));
        assertEquals(expectedSql, result);
    }

    @Test
    @DisplayName("Test 4: Gemini returns INVALID - service throws InvalidQuestionException")
    void generateSql_invalidReturnsException() {
        when(callResponseSpec.content()).thenReturn("INVALID");

        InvalidQuestionException ex = assertThrows(
                InvalidQuestionException.class,
                () -> textToSqlService.generateSql("Show me employee salary information")
        );

        assertEquals("The question cannot be answered using the available database schema.", ex.getMessage());
        assertEquals("Show me employee salary information", ex.getQuestion());
    }

    @Test
    @DisplayName("Test 5: Gemini returns SQL inside Markdown fences - fences are stripped")
    void generateSql_stripsMarkdownFences() {
        String markdownResponse = "```sql\nSELECT * FROM customers;\n```";
        when(callResponseSpec.content()).thenReturn(markdownResponse);

        String result = textToSqlService.generateSql("Show all customers");

        assertEquals("SELECT * FROM customers;", result);
    }

    @Test
    @DisplayName("Test 5b: Gemini returns SQL with conversational prefix - prefix is stripped")
    void generateSql_stripsIntroductoryText() {
        String conversationalResponse = "Here is the SQL:\nSELECT * FROM customers;";
        when(callResponseSpec.content()).thenReturn(conversationalResponse);

        String result = textToSqlService.generateSql("Show all customers");

        assertEquals("SELECT * FROM customers;", result);
    }

    @Test
    @DisplayName("Test 6: Gemini throws an exception - service throws AiServiceException without leaking provider details")
    void generateSql_providerFailure_throwsAiServiceException() {
        when(callResponseSpec.content()).thenThrow(new RuntimeException("Google Gemini API error: 429 Too Many Requests"));

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> textToSqlService.generateSql("Show all customers")
        );

        assertEquals("AI service is temporarily unavailable", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("generateSql rejects null or blank question")
    void generateSql_blankQuestion_throws() {
        AiServiceException ex1 = assertThrows(
                AiServiceException.class,
                () -> textToSqlService.generateSql("")
        );
        assertTrue(ex1.getMessage().contains("must not be null or blank"));

        AiServiceException ex2 = assertThrows(
                AiServiceException.class,
                () -> textToSqlService.generateSql(null)
        );
        assertTrue(ex2.getMessage().contains("must not be null or blank"));

        verifyNoInteractions(chatClient);
    }

    @Test
    @DisplayName("generateSql throws when Gemini returns empty response")
    void generateSql_emptyResponse_throws() {
        when(callResponseSpec.content()).thenReturn("   ");

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> textToSqlService.generateSql("Show all customers")
        );

        assertEquals("AI service returned an empty response", ex.getMessage());
    }
}
