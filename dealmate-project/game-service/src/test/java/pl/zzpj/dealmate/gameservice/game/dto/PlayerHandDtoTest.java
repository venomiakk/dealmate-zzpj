package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHandDtoTest {

    @Test
    void shouldCreatePlayerHandDtoCorrectly() {

        String playerId = "player1";
        CardDto card = new CardDto("AS", "ACE", "SPADES", new ImageLinksDto("svg_link", "png_link"));
        List<CardDto> cards = List.of(card);
        int value = 21;
        String status = "BLACKJACK";


        PlayerHandDto playerHandDto = new PlayerHandDto(playerId, cards, value, status);


        assertEquals(playerId, playerHandDto.playerId());
        assertEquals(cards, playerHandDto.cards());
        assertEquals(value, playerHandDto.value());
        assertEquals(status, playerHandDto.status());
    }
}
