package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PileDTOTest {

    @Test
    void shouldSetAndGetAllFields() {
        PileDTO pile = new PileDTO();
        pile.setPileName("pile1");
        pile.setRemainingCards(3);
        pile.setCardCodes(List.of("3H", "4C", "5S"));

        assertThat(pile.getPileName()).isEqualTo("pile1");
        assertThat(pile.getRemainingCards()).isEqualTo(3);
        assertThat(pile.getCardCodes()).containsExactly("3H", "4C", "5S");
    }
}
