package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PileDTOTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        PileDTO pile = new PileDTO();
        pile.setPileName("discardPile");
        pile.setRemainingCards(10);
        pile.setCardCodes(List.of("AS", "KH", "3D"));

        assertThat(pile.getPileName()).isEqualTo("discardPile");
        assertThat(pile.getRemainingCards()).isEqualTo(10);
        assertThat(pile.getCardCodes()).containsExactly("AS", "KH", "3D");
    }

    @Test
    void shouldHandleNullCardCodes() {
        PileDTO pile = new PileDTO();
        pile.setCardCodes(null);

        assertThat(pile.getCardCodes()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        PileDTO pile1 = new PileDTO();
        pile1.setPileName("testPile");
        pile1.setRemainingCards(5);
        pile1.setCardCodes(List.of("AS", "2H"));

        PileDTO pile2 = new PileDTO();
        pile2.setPileName("testPile");
        pile2.setRemainingCards(5);
        pile2.setCardCodes(List.of("AS", "2H"));

        assertThat(pile1).isEqualTo(pile2);
        assertThat(pile1.hashCode()).isEqualTo(pile2.hashCode());
    }

    @Test
    void shouldVerifyToString() {
        PileDTO pile = new PileDTO();
        pile.setPileName("pileA");
        pile.setRemainingCards(3);
        pile.setCardCodes(List.of("QD", "7S"));

        String toString = pile.toString();
        assertThat(toString).contains("pileA", "3", "QD", "7S");
    }
}
