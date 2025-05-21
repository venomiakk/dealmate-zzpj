package pl.zzpj.dealmate.deckservice.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AddToPileApiResponse {
    private boolean success;

    @JsonProperty("deck_id")
    private String deckId;

    private int remaining;

    private Map<String, PileDetails> piles;

    @Data
    public static class PileDetails {
        private int remaining;
    }
}
