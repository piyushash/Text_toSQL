package com.piyush.aitexttosql.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI wiring. Reuses Spring AI's auto-configured
 * {@link ChatClient.Builder} and exposes a single reusable {@link ChatClient}
 * bean. No provider-specific logic lives here.
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
