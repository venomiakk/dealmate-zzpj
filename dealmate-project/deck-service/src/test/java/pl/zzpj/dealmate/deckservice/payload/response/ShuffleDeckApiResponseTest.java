package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShuffleDeckApiResponseTest {

    @Test
    void shouldSetAndGetFieldsProperly() {

        ShuffleDeckApiResponse response = new ShuffleDeckApiResponse();
        response.setDeckId("deck123");
        response.setShuffled(true);
        response.setRemainingCards(52);


        assertThat(response.getDeckId()).isEqualTo("deck123");
        assertThat(response.isShuffled()).isTrue();
        assertThat(response.getRemainingCards()).isEqualTo(52);
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {

        ShuffleDeckApiResponse response1 = new ShuffleDeckApiResponse();
        response1.setDeckId("deckABC");
        response1.setShuffled(false);
        response1.setRemainingCards(15);

        ShuffleDeckApiResponse response2 = new ShuffleDeckApiResponse();
        response2.setDeckId("deckABC");
        response2.setShuffled(false);
        response2.setRemainingCards(15);


        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void shouldVerifyToString() {

        ShuffleDeckApiResponse response = new ShuffleDeckApiResponse();
        response.setDeckId("deckXYZ");
        response.setShuffled(true);
        response.setRemainingCards(30);


        String toString = response.toString();
        assertThat(toString).contains("deckXYZ", "true", "30");
    }
}
