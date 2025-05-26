package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckDTOTest {

    @Test
    void shouldSetAndGetAllFields() {
        DeckDTO deck = new DeckDTO();
        deck.setDeckId("abc123");
        deck.setShuffled(true);
        deck.setRemainingCards(42);
        deck.setDrawnCardCodes(List.of("AS", "2H"));

        assertThat(deck.getDeckId()).isEqualTo("abc123");
        assertThat(deck.isShuffled()).isTrue();
        assertThat(deck.getRemainingCards()).isEqualTo(42);
        assertThat(deck.getDrawnCardCodes()).containsExactly("AS", "2H");
    }
}
