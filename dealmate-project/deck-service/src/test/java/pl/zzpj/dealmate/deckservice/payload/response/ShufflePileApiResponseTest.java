package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.dto.ImageLinksDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShufflePileApiResponseTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        CardDTO card = new CardDTO();
        card.setCode("AS");
        card.setValue("Ace");
        card.setSuit("Spades");
        ImageLinksDTO images = new ImageLinksDTO();
        images.setPng("pngUrl");
        images.setSvg("svgUrl");
        card.setImages(images);

        ShufflePileApiResponse.PileInfo pileInfo = new ShufflePileApiResponse.PileInfo();
        pileInfo.setRemaining(5);

        Map<String, ShufflePileApiResponse.PileInfo> piles = new HashMap<>();
        piles.put("discardPile", pileInfo);

        ShufflePileApiResponse response = new ShufflePileApiResponse();
        response.setSuccess(true);
        response.setDeck_id("deck123");
        response.setRemaining(42);
        response.setPiles(piles);
        response.setCards(List.of(card));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDeck_id()).isEqualTo("deck123");
        assertThat(response.getRemaining()).isEqualTo(42);
        assertThat(response.getPiles()).containsKey("discardPile");
        assertThat(response.getPiles().get("discardPile").getRemaining()).isEqualTo(5);
        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().get(0).getCode()).isEqualTo("AS");
        assertThat(response.getCards().get(0).getImages().getPng()).isEqualTo("pngUrl");
    }

    @Test
    void shouldHandleNullFields() {
        ShufflePileApiResponse response = new ShufflePileApiResponse();
        response.setPiles(null);
        response.setCards(null);

        assertThat(response.getPiles()).isNull();
        assertThat(response.getCards()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCodeForPileInfo() {
        ShufflePileApiResponse.PileInfo pile1 = new ShufflePileApiResponse.PileInfo();
        pile1.setRemaining(5);

        ShufflePileApiResponse.PileInfo pile2 = new ShufflePileApiResponse.PileInfo();
        pile2.setRemaining(5);

        assertThat(pile1).isEqualTo(pile2);
        assertThat(pile1.hashCode()).isEqualTo(pile2.hashCode());
    }

    @Test
    void shouldVerifyToStringForPileInfo() {
        ShufflePileApiResponse.PileInfo pile = new ShufflePileApiResponse.PileInfo();
        pile.setRemaining(3);

        String toString = pile.toString();
        assertThat(toString).contains("3");
    }
}
