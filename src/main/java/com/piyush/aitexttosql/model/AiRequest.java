package com.piyush.aitexttosql.model;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Request body for the AI chat endpoint.
 */
public class AiRequest {

    @NotBlank(message = "message must not be null or blank")
    private String message;

    public AiRequest() {
    }

    public AiRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiRequest aiRequest = (AiRequest) o;
        return Objects.equals(message, aiRequest.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "AiRequest{"
                + "message='" + message + '\''
                + '}';
    }
}
