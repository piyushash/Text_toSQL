package com.piyush.aitexttosql.service;

import com.piyush.aitexttosql.exception.AiServiceException;
import com.piyush.aitexttosql.exception.InvalidQuestionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for converting natural-language questions
 * into valid PostgreSQL SELECT queries using Google Gemini via Spring AI's ChatClient.
 */
@Service
public class TextToSqlService {

    private static final Logger log = LoggerFactory.getLogger(TextToSqlService.class);

    private static final String SYSTEM_PROMPT = """
            You are a PostgreSQL SQL generation assistant.
            Your job is to convert natural-language questions into valid PostgreSQL SQL.

            DATABASE SCHEMA:

            Table: customers
            - id INTEGER PRIMARY KEY
            - name VARCHAR(100)
            - city VARCHAR(100)
            - email VARCHAR(150)
            - created_at TIMESTAMP

            Table: orders
            - id INTEGER PRIMARY KEY
            - customer_id INTEGER
            - amount NUMERIC(12,2)
            - order_date DATE

            Relationship:
            orders.customer_id references customers.id

            Rules:
            1. Generate ONLY SQL.
            2. Do not explain the SQL.
            3. Do not use Markdown.
            4. Do not wrap SQL in ```sql.
            5. Use only tables and columns provided in the schema.
            6. Generate PostgreSQL-compatible SQL.
            7. Do not invent tables or columns.
            8. For questions involving customers and orders, use the correct JOIN.
            9. Prefer explicit column names when practical.
            10. Do not execute the query.
            11. Do not modify database data.
            12. Do not generate INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE, CREATE, GRANT, or REVOKE statements.
            13. Generate read-only SELECT queries only.
            14. If the question cannot be answered using the provided schema, return:
            INVALID
            15. Return one SQL statement only.
            """;

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:sql)?\\s*([\\s\\S]*?)\\s*```", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTRO_TEXT_PATTERN = Pattern.compile("^(?:Here is the (?:SQL|query)[^:]*:\\s*)+", Pattern.CASE_INSENSITIVE);

    private final ChatClient chatClient;

    public TextToSqlService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Converts a natural-language question into a PostgreSQL SQL query.
     *
     * @param question the natural language question
     * @return generated SQL query string
     * @throws InvalidQuestionException if the question cannot be answered by the schema
     * @throws AiServiceException if the AI provider fails or returns invalid response
     */
    public String generateSql(String question) {
        log.info("Text-to-SQL request received");

        if (!StringUtils.hasText(question)) {
            throw new AiServiceException("question must not be null or blank");
        }

        String rawResponse;
        try {
            rawResponse = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(question)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("Text-to-SQL generation failed", ex);
            throw new AiServiceException("AI service is temporarily unavailable", ex);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("AI returned an empty response for text-to-sql");
            throw new AiServiceException("AI service returned an empty response");
        }

        String cleanedSql = cleanSql(rawResponse);

        if ("INVALID".equalsIgnoreCase(cleanedSql) || cleanedSql.toUpperCase().startsWith("INVALID")) {
            log.warn("Question cannot be answered using the available schema");
            throw new InvalidQuestionException(question, "The question cannot be answered using the available database schema.");
        }

        log.info("Text-to-SQL SQL generated successfully");
        return cleanedSql;
    }

    /**
     * Normalizes the AI response by stripping markdown code fences,
     * language specifiers, and unwanted conversational prefixes.
     */
    String cleanSql(String response) {
        if (response == null) {
            return "";
        }
        String cleaned = response.trim();

        // Strip markdown code fences if present
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        // Strip conversational prefixes like "Here is the SQL:"
        cleaned = INTRO_TEXT_PATTERN.matcher(cleaned).replaceFirst("").trim();

        // Strip any remaining backticks
        if (cleaned.startsWith("`") && cleaned.endsWith("`")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }

        return cleaned;
    }
}
