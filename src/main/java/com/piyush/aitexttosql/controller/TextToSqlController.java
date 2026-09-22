package com.piyush.aitexttosql.controller;

import com.piyush.aitexttosql.model.TextToSqlRequest;
import com.piyush.aitexttosql.model.TextToSqlResponse;
import com.piyush.aitexttosql.service.TextToSqlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for natural-language to SQL translation.
 * Receives the user question, delegates to {@link TextToSqlService},
 * and returns the generated SQL without executing it.
 */
@RestController
@RequestMapping("/api/text-to-sql")
public class TextToSqlController {

    private final TextToSqlService textToSqlService;

    public TextToSqlController(TextToSqlService textToSqlService) {
        this.textToSqlService = textToSqlService;
    }

    @PostMapping
    public ResponseEntity<TextToSqlResponse> generateSql(@Valid @RequestBody TextToSqlRequest request) {
        String sql = textToSqlService.generateSql(request.getQuestion());
        return ResponseEntity.ok(new TextToSqlResponse(request.getQuestion(), sql));
    }
}
