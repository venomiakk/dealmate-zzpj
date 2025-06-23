package pl.zzpj.dealmate.deckservice.payload.response;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AddToPileApiResponseTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        AddToPileApiResponse response = new AddToPileApiResponse();
        response.setSuccess(true);
        response.setDeckId("deck123");
        response.setRemaining(15);

        AddToPileApiResponse.PileDetails pileDetails = new AddToPileApiResponse.PileDetails();
        pileDetails.setRemaining(5);

        Map<String, AddToPileApiResponse.PileDetails> pilesMap = new HashMap<>();
        pilesMap.put("discardPile", pileDetails);
        response.setPiles(pilesMap);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDeckId()).isEqualTo("deck123");
        assertThat(response.getRemaining()).isEqualTo(15);
        assertThat(response.getPiles()).containsKey("discardPile");
        assertThat(response.getPiles().get("discardPile").getRemaining()).isEqualTo(5);
    }

    @Test
    void shouldHandleNullPiles() {

        AddToPileApiResponse response = new AddToPileApiResponse();
        response.setPiles(null);


        assertThat(response.getPiles()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCodeForPileDetails() {

        AddToPileApiResponse.PileDetails pileDetails1 = new AddToPileApiResponse.PileDetails();
        pileDetails1.setRemaining(7);

        AddToPileApiResponse.PileDetails pileDetails2 = new AddToPileApiResponse.PileDetails();
        pileDetails2.setRemaining(7);


        assertThat(pileDetails1).isEqualTo(pileDetails2);
        assertThat(pileDetails1.hashCode()).isEqualTo(pileDetails2.hashCode());
    }

    @Test
    void shouldVerifyToStringForPileDetails() {

        AddToPileApiResponse.PileDetails pileDetails = new AddToPileApiResponse.PileDetails();
        pileDetails.setRemaining(3);

        String toString = pileDetails.toString();
        assertThat(toString).contains("3");
    }
}
