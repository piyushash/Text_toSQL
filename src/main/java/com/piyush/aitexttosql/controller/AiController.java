package com.piyush.aitexttosql.controller;

import com.piyush.aitexttosql.model.AiRequest;
import com.piyush.aitexttosql.model.AiResponse;
import com.piyush.aitexttosql.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for AI chat.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiResponse> chat(@Valid @RequestBody AiRequest request) {
        String response = aiService.generateResponse(request.getMessage());
        return new ResponseEntity<>(new AiResponse(request.getMessage(), response), HttpStatus.OK);
    }
}
