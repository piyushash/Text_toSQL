package com.piyush.aitexttosql.service;

import com.piyush.aitexttosql.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Business logic layer for AI chat. Talks to the LLM exclusively through
 * Spring AI's {@link ChatClient} abstraction.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;

    public AiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Sends a natural-language message to the LLM and returns the generated
     * text. Provider/transport failures are wrapped in an
     * {@link AiServiceException} so controllers can respond with a clean
     * error body.
     */
    public String generateResponse(String message) {
        log.info("AI request received");
        if (!StringUtils.hasText(message)) {
            throw new AiServiceException("message must not be null or blank");
        }

        String content;
        try {
            content = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("AI provider error", ex);
            throw new AiServiceException("AI service is temporarily unavailable", ex);
        }

        if (content == null || content.isBlank()) {
            log.warn("AI returned an empty response");
            throw new AiServiceException("AI service returned an empty response");
        }

        log.info("AI response generated successfully");
        return content.trim();
    }
}
