package com.piyush.aitexttosql.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Response payload containing the natural-language question, the generated SQL,
 * and an optional error message if the query cannot be generated.
 */
public class TextToSqlResponse {

    private String question;
    private String sql;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public TextToSqlResponse() {
    }

    public TextToSqlResponse(String question, String sql) {
        this.question = question;
        this.sql = sql;
    }

    public TextToSqlResponse(String question, String sql, String error) {
        this.question = question;
        this.sql = sql;
        this.error = error;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TextToSqlResponse that = (TextToSqlResponse) o;
        return Objects.equals(question, that.question)
                && Objects.equals(sql, that.sql)
                && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, sql, error);
    }

    @Override
    public String toString() {
        return "TextToSqlResponse{"
                + "question='" + question + '\''
                + ", sql='" + sql + '\''
                + ", error='" + error + '\''
                + '}';
    }
}
