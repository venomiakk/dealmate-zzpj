package pl.zzpj.dealmate.aiservice.service;

import pl.zzpj.dealmate.aiservice.dto.CardDto;
import pl.zzpj.dealmate.aiservice.dto.PokerAiRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class PromptBuilder {

    public static String buildPrompt(PokerAiRequest req) {
        String hand = formatCards(req.hand());
        String table = formatCards(req.tableCards());
        String game = req.gameType().name();

        return String.format("""
            You're a poker expert AI. Game type: %s.
            Player's hand: %s
            Cards on table: %s
            Based on the hand and situation, what is the best move: FOLD, CALL, or RAISE? 
            Answer with only the move.
        """, game, hand, table);
    }

    private static String formatCards(Iterable<CardDto> cards) {
        if (cards == null || !cards.iterator().hasNext()) {
            return "none";
        }

        List<String> cardStrings = new ArrayList<>();
        for (CardDto card : cards) {
            cardStrings.add(card.rank() + " of " + card.suit());
        }

        return String.join(", ", cardStrings);
    }
}