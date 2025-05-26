package pl.zzpj.dealmate.deckservice.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReturnCardsApiResponse {
    private boolean success;

    @JsonProperty("deck_id")
    private String deckId;

    private int remaining;
}
