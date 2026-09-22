package com.piyush.aitexttosql.model;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Request payload for the natural-language to SQL conversion endpoint.
 */
public class TextToSqlRequest {

    @NotBlank(message = "question must not be null or blank")
    private String question;

    public TextToSqlRequest() {
    }

    public TextToSqlRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TextToSqlRequest that = (TextToSqlRequest) o;
        return Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question);
    }

    @Override
    public String toString() {
        return "TextToSqlRequest{"
                + "question='" + question + '\''
                + '}';
    }
}
