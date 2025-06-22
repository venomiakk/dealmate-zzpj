package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckDTOTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        DeckDTO deck = new DeckDTO();
        deck.setId(1L);
        deck.setDeckId("testDeckId");
        deck.setShuffled(true);
        deck.setRemainingCards(42);
        deck.setDrawnCardCodes(List.of("AS", "KH", "2D"));

        assertThat(deck.getId()).isEqualTo(1L);
        assertThat(deck.getDeckId()).isEqualTo("testDeckId");
        assertThat(deck.isShuffled()).isTrue();
        assertThat(deck.getRemainingCards()).isEqualTo(42);
        assertThat(deck.getDrawnCardCodes()).containsExactly("AS", "KH", "2D");
    }

    @Test
    void shouldHandleNullDrawnCardCodes() {
        DeckDTO deck = new DeckDTO();
        deck.setDrawnCardCodes(null);

        assertThat(deck.getDrawnCardCodes()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        DeckDTO deck1 = new DeckDTO();
        deck1.setId(1L);
        deck1.setDeckId("deck123");
        deck1.setShuffled(true);
        deck1.setRemainingCards(10);
        deck1.setDrawnCardCodes(List.of("AS", "2S"));

        DeckDTO deck2 = new DeckDTO();
        deck2.setId(1L);
        deck2.setDeckId("deck123");
        deck2.setShuffled(true);
        deck2.setRemainingCards(10);
        deck2.setDrawnCardCodes(List.of("AS", "2S"));

        assertThat(deck1).isEqualTo(deck2);
        assertThat(deck1.hashCode()).isEqualTo(deck2.hashCode());
    }

    @Test
    void shouldVerifyToString() {
        DeckDTO deck = new DeckDTO();
        deck.setId(1L);
        deck.setDeckId("deck456");
        deck.setShuffled(false);
        deck.setRemainingCards(5);
        deck.setDrawnCardCodes(List.of("9C", "7H"));

        String toString = deck.toString();
        assertThat(toString).contains("deck456", "false", "5", "9C", "7H");
    }
}
