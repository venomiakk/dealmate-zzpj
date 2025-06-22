package pl.zzpj.dealmate.deckservice.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PileEntityTest {

    @Test
    void shouldSetAndGetFieldsProperly() {

        DeckEntity deck = new DeckEntity();
        deck.setId(1L);
        deck.setDeckId("deck123");

        PileEntity pile = new PileEntity();
        pile.setId(10L);
        pile.setPileName("discardPile");
        pile.setDeck(deck);
        pile.setRemainingCards(5);
        pile.setCardCodes(List.of("AS", "KH", "3D"));


        assertThat(pile.getId()).isEqualTo(10L);
        assertThat(pile.getPileName()).isEqualTo("discardPile");
        assertThat(pile.getDeck()).isEqualTo(deck);
        assertThat(pile.getRemainingCards()).isEqualTo(5);
        assertThat(pile.getCardCodes()).containsExactly("AS", "KH", "3D");
    }

    @Test
    void shouldHandleNullCardCodes() {

        PileEntity pile = new PileEntity();
        pile.setCardCodes(null);


        assertThat(pile.getCardCodes()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {

        DeckEntity deck = new DeckEntity();
        deck.setId(2L);
        deck.setDeckId("deckABC");

        PileEntity pile1 = new PileEntity();
        pile1.setId(1L);
        pile1.setPileName("pileA");
        pile1.setDeck(deck);
        pile1.setRemainingCards(3);
        pile1.setCardCodes(List.of("9C", "7H"));

        PileEntity pile2 = new PileEntity();
        pile2.setId(1L);
        pile2.setPileName("pileA");
        pile2.setDeck(deck);
        pile2.setRemainingCards(3);
        pile2.setCardCodes(List.of("9C", "7H"));


        assertThat(pile1).isEqualTo(pile2);
        assertThat(pile1.hashCode()).isEqualTo(pile2.hashCode());
    }

    @Test
    void shouldVerifyToString() {

        PileEntity pile = new PileEntity();
        pile.setId(3L);
        pile.setPileName("pileXYZ");
        pile.setRemainingCards(8);
        pile.setCardCodes(List.of("5S", "QC"));

        String toString = pile.toString();
        assertThat(toString).contains("pileXYZ", "8", "5S", "QC");
    }
}
