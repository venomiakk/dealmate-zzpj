package pl.zzpj.dealmate.aiservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.aiservice.dto.PokerAiRequest;
import pl.zzpj.dealmate.aiservice.service.AiSuggestionService;
@Slf4j
@RestController
@RequestMapping("/ai")
@ComponentScan(basePackages = {"pl.zzpj.dealmate.aiservice.service"})
public class AiSuggestionController {

    private final AiSuggestionService service;

    public AiSuggestionController(AiSuggestionService service) {

        this.service = service;
    }

    @PostMapping("/suggest-move")
    public ResponseEntity<String> suggestMove(@RequestBody PokerAiRequest request) {
        log.warn("Received AI move request: {}", request);
        // In real life return a typed response object, e.g. AiSuggestionResponse
        String move = service.getBestMove(request);
        return ResponseEntity.ok(move);
    }
}
