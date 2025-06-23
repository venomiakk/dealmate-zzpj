package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.dto.ImageLinksDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ListPileApiResponseTest {

    @Test
    void shouldSetAndGetFieldsProperly() {

        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        card1.setValue("Ace");
        card1.setSuit("Spades");
        ImageLinksDTO images = new ImageLinksDTO();
        images.setPng("pngUrl");
        images.setSvg("svgUrl");
        card1.setImages(images);

        ListPileApiResponse.Pile pile = new ListPileApiResponse.Pile();
        pile.setCards(List.of(card1));
        pile.setRemaining(3);

        Map<String, ListPileApiResponse.Pile> pilesMap = new HashMap<>();
        pilesMap.put("discardPile", pile);

        ListPileApiResponse response = new ListPileApiResponse();
        response.setSuccess(true);
        response.setDeckId("deck123");
        response.setPiles(pilesMap);


        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDeckId()).isEqualTo("deck123");
        assertThat(response.getPiles()).containsKey("discardPile");
        assertThat(response.getPiles().get("discardPile").getRemaining()).isEqualTo(3);
        assertThat(response.getPiles().get("discardPile").getCards()).hasSize(1);
        assertThat(response.getPiles().get("discardPile").getCards().get(0).getCode()).isEqualTo("AS");
        assertThat(response.getPiles().get("discardPile").getCards().get(0).getImages().getPng()).isEqualTo("pngUrl");
    }

    @Test
    void shouldHandleNullPiles() {

        ListPileApiResponse response = new ListPileApiResponse();
        response.setPiles(null);


        assertThat(response.getPiles()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCodeForPile() {

        ListPileApiResponse.Pile pile1 = new ListPileApiResponse.Pile();
        pile1.setRemaining(5);

        ListPileApiResponse.Pile pile2 = new ListPileApiResponse.Pile();
        pile2.setRemaining(5);

        assertThat(pile1).isEqualTo(pile2);
        assertThat(pile1.hashCode()).isEqualTo(pile2.hashCode());
    }

    @Test
    void shouldVerifyToStringForPile() {

        ListPileApiResponse.Pile pile = new ListPileApiResponse.Pile();
        pile.setRemaining(2);


        String toString = pile.toString();
        assertThat(toString).contains("2");
    }
}
