package pl.zzpj.dealmate.aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import java.util.Map;

@Component
public class GroqClient {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${base_url}")
    private String GROQ_API_URL;
    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;

    public String getAiMove(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "meta-llama/llama-4-scout-17b-16e-instruct",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You're a poker move advisor."),
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.2
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_API_URL, request, Map.class);

        var content = ((Map)((Map)((java.util.List)response.getBody().get("choices")).get(0)).get("message")).get("content");
        return content.toString().trim();
    }
}
