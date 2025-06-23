package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckDtoTest {

    @Test
    void shouldCreateDeckDtoCorrectly() {

        String deckId = "abc123";
        boolean shuffled = true;
        int remainingCards = 52;
        long id = 1L;


        DeckDto dto = new DeckDto(deckId, shuffled, remainingCards, id);


        assertEquals(deckId, dto.deckId());
        assertTrue(dto.shuffled());
        assertEquals(remainingCards, dto.remainingCards());
        assertEquals(id, dto.id());
    }
}
