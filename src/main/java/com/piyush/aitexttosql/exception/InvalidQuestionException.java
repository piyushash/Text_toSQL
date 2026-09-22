package com.piyush.aitexttosql.exception;

/**
 * Thrown when a natural-language question cannot be answered
 * using the provided database schema (e.g. references non-existent tables).
 */
public class InvalidQuestionException extends RuntimeException {

    private final String question;

    public InvalidQuestionException(String question, String message) {
        super(message);
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
