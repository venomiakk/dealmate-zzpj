package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.dto.ImageLinksDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrawCardsApiResponseTest {

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

        DrawCardsApiResponse response = new DrawCardsApiResponse();
        response.setSuccess(true);
        response.setDeckId("deck123");
        response.setCards(List.of(card1));
        response.setRemaining(51);


        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDeckId()).isEqualTo("deck123");
        assertThat(response.getRemaining()).isEqualTo(51);
        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().get(0).getCode()).isEqualTo("AS");
        assertThat(response.getCards().get(0).getImages().getPng()).isEqualTo("pngUrl");
    }

    @Test
    void shouldHandleNullCards() {

        DrawCardsApiResponse response = new DrawCardsApiResponse();
        response.setCards(null);


        assertThat(response.getCards()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {

        DrawCardsApiResponse response1 = new DrawCardsApiResponse();
        response1.setSuccess(true);
        response1.setDeckId("deckABC");
        response1.setRemaining(10);

        DrawCardsApiResponse response2 = new DrawCardsApiResponse();
        response2.setSuccess(true);
        response2.setDeckId("deckABC");
        response2.setRemaining(10);


        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void shouldVerifyToString() {

        DrawCardsApiResponse response = new DrawCardsApiResponse();
        response.setSuccess(false);
        response.setDeckId("deckXYZ");
        response.setRemaining(20);


        String toString = response.toString();
        assertThat(toString).contains("deckXYZ", "false", "20");
    }
}
