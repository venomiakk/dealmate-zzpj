package pl.zzpj.dealmate.aiservice.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiContriller {

    private final ChatClient chatClient;

    @Autowired
    public AiContriller(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    @GetMapping("/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        String response = chatClient.prompt().user(message).call().content();
        return Map.of("generation", response);
    }
    @GetMapping("/generateStream")
    public Flux<String> generateStream(@RequestParam(value = "message",
            defaultValue = "Tell me a joke") String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}
