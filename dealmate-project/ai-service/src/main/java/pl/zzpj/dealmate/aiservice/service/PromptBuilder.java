package pl.zzpj.dealmate.aiservice.service;

import pl.zzpj.dealmate.aiservice.dto.PokerAiRequest;

import java.util.ArrayList;
import java.util.List;

public class PromptBuilder {

    public static String buildPrompt(PokerAiRequest req) {
        String hand = String.valueOf(req.hand());
        String table = String.valueOf(req.dealer());

        return String.format("""
            You're a blackjack expert AI.\
            Player's hand: %s
            Cards on table: %s
            Based on the hand and situation, what is the best move: hit, stand? 
            Answer with only the move.
        """, hand, table);
    }


}