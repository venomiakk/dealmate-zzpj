package pl.zzpj.dealmate.deckservice.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeckEntityTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        DeckEntity entity = new DeckEntity();
        entity.setId(1L);
        entity.setDeckId("deck123");
        entity.setShuffled(true);
        entity.setRemainingCards(52);
        entity.setDrawnCardCodes(List.of("AS", "KH", "3D"));

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getDeckId()).isEqualTo("deck123");
        assertThat(entity.isShuffled()).isTrue();
        assertThat(entity.getRemainingCards()).isEqualTo(52);
        assertThat(entity.getDrawnCardCodes()).containsExactly("AS", "KH", "3D");
    }

    @Test
    void shouldInitializeDrawnCardCodesListByDefault() {
        DeckEntity entity = new DeckEntity();

        assertThat(entity.getDrawnCardCodes()).isNotNull();
        assertThat(entity.getDrawnCardCodes()).isEmpty();
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        DeckEntity entity1 = new DeckEntity();
        entity1.setId(1L);
        entity1.setDeckId("deck123");
        entity1.setShuffled(true);
        entity1.setRemainingCards(10);
        entity1.setDrawnCardCodes(List.of("AS", "2S"));

        DeckEntity entity2 = new DeckEntity();
        entity2.setId(1L);
        entity2.setDeckId("deck123");
        entity2.setShuffled(true);
        entity2.setRemainingCards(10);
        entity2.setDrawnCardCodes(List.of("AS", "2S"));

        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    void shouldVerifyToString() {
        DeckEntity entity = new DeckEntity();
        entity.setId(2L);
        entity.setDeckId("deckABC");
        entity.setShuffled(false);
        entity.setRemainingCards(30);
        entity.setDrawnCardCodes(List.of("9C", "7H"));


        String toString = entity.toString();
        assertThat(toString).contains("deckABC", "false", "30", "9C", "7H");
    }
}
