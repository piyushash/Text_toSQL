package com.piyush.aitexttosql.exception;

/**
 * Signals a failure in the AI service layer. Internal details (stack traces,
 * provider credentials) are never exposed to clients; only handled here.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
