package pl.zzpj.dealmate.aiservice.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import pl.zzpj.dealmate.aiservice.dto.PokerAiRequest;

@Service
public class AiSuggestionService {

    private final GroqClient aiClient;

    public AiSuggestionService(GroqClient aiClient) {
        this.aiClient = aiClient;
    }

    public String getBestMove(PokerAiRequest req) {
        String prompt = PromptBuilder.buildPrompt(req);
        return aiClient.getAiMove(prompt);
    }
}