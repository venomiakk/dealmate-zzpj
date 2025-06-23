package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardDtoTest {

    @Test
    void shouldCreateCardDtoCorrectly() {

        String code = "AS";
        String value = "Ace";
        String suit = "Spades";
        ImageLinksDto images = new ImageLinksDto("https://example.com/as.svg", "https://example.com/as.png");


        CardDto dto = new CardDto(code, value, suit, images);


        assertEquals(code, dto.code());
        assertEquals(value, dto.value());
        assertEquals(suit, dto.suit());
        assertNotNull(dto.images());
        assertEquals("https://example.com/as.svg", dto.images().svg());
        assertEquals("https://example.com/as.png", dto.images().png());
    }
}
