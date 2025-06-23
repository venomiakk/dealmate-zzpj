package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnCardsApiResponseTest {

    @Test
    void shouldSetAndGetFieldsProperly() {

        ReturnCardsApiResponse response = new ReturnCardsApiResponse();
        response.setSuccess(true);
        response.setDeckId("deck123");
        response.setRemaining(42);


        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDeckId()).isEqualTo("deck123");
        assertThat(response.getRemaining()).isEqualTo(42);
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {

        ReturnCardsApiResponse response1 = new ReturnCardsApiResponse();
        response1.setSuccess(true);
        response1.setDeckId("deckABC");
        response1.setRemaining(10);

        ReturnCardsApiResponse response2 = new ReturnCardsApiResponse();
        response2.setSuccess(true);
        response2.setDeckId("deckABC");
        response2.setRemaining(10);


        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void shouldVerifyToString() {

        ReturnCardsApiResponse response = new ReturnCardsApiResponse();
        response.setSuccess(false);
        response.setDeckId("deckXYZ");
        response.setRemaining(20);


        String toString = response.toString();
        assertThat(toString).contains("deckXYZ", "false", "20");
    }
}
