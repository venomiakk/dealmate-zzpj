package pl.zzpj.dealmate.aiservice.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.aiservice.dto.PokerAiRequest;
import pl.zzpj.dealmate.aiservice.service.AiSuggestionService;

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
        // In real life return a typed response object, e.g. AiSuggestionResponse
        String move = service.getBestMove(request);
        return ResponseEntity.ok(move);
    }
}
