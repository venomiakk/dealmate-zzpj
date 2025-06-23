package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameStateDtoTest {

    @Test
    void shouldCreateGameStateDtoCorrectly() {
        CardDto card = new CardDto("AS", "ACE", "SPADES", new ImageLinksDto("svg_link", "png_link"));
        List<CardDto> dealerCards = List.of(card);
        List<CardDto> playerCards = List.of(card, card);


        PlayerHandDto dealerHand = new PlayerHandDto("dealer", dealerCards, 17, "PLAYING");
        PlayerHandDto playerHand = new PlayerHandDto("player1", playerCards, 21, "BLACKJACK");


        Map<String, PlayerHandDto> playerHands = Map.of("player1", playerHand);


        String currentPlayerId = "player1";
        BigDecimal pot = BigDecimal.valueOf(100);
        List<String> winners = List.of("player1");
        String message = "Round finished";
        Integer countdown = 5;


        GameStateDto dto = new GameStateDto(
                "IN_PROGRESS",
                playerHands,
                dealerHand,
                currentPlayerId,
                pot,
                winners,
                message,
                countdown
        );


        assertEquals("IN_PROGRESS", dto.gameStatus());
        assertEquals(playerHands, dto.playerHands());
        assertEquals(dealerHand, dto.dealerHand());
        assertEquals(currentPlayerId, dto.currentPlayerId());
        assertEquals(pot, dto.pot());
        assertEquals(winners, dto.winners());
        assertEquals(message, dto.message());
        assertEquals(countdown, dto.nextRoundCountdown());
    }
}
