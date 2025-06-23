package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeckApiResponseTest {

    @Test
    void shouldSetAndGetFieldsProperly() {

        CreateDeckApiResponse response = new CreateDeckApiResponse();
        response.setDeckId("deck123");
        response.setShuffled(true);
        response.setRemainingCards(52);


        assertThat(response.getDeckId()).isEqualTo("deck123");
        assertThat(response.isShuffled()).isTrue();
        assertThat(response.getRemainingCards()).isEqualTo(52);
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {

        CreateDeckApiResponse response1 = new CreateDeckApiResponse();
        response1.setDeckId("deckABC");
        response1.setShuffled(false);
        response1.setRemainingCards(10);

        CreateDeckApiResponse response2 = new CreateDeckApiResponse();
        response2.setDeckId("deckABC");
        response2.setShuffled(false);
        response2.setRemainingCards(10);


        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void shouldVerifyToString() {

        CreateDeckApiResponse response = new CreateDeckApiResponse();
        response.setDeckId("deckXYZ");
        response.setShuffled(false);
        response.setRemainingCards(20);


        String toString = response.toString();
        assertThat(toString).contains("deckXYZ", "false", "20");
    }
}
