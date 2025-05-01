package pl.zzpj.dealmate.deckservice.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateDeckApiResponse {
    @JsonProperty("deck_id")
    private String deckId;
    private boolean shuffled;
    @JsonProperty("remaining")
    private int remainingCards;
}
