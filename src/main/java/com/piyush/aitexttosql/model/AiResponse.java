package com.piyush.aitexttosql.model;

import java.util.Objects;

/**
 * Response body for the AI chat endpoint.
 */
public class AiResponse {

    private String message;
    private String response;

    public AiResponse() {
    }

    public AiResponse(String message, String response) {
        this.message = message;
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiResponse that = (AiResponse) o;
        return Objects.equals(message, that.message)
                && Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, response);
    }

    @Override
    public String toString() {
        return "AiResponse{"
                + "message='" + message + '\''
                + ", response='" + response + '\''
                + '}';
    }
}
