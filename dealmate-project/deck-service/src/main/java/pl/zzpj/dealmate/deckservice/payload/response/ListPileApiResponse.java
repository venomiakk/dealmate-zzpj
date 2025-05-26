package pl.zzpj.dealmate.deckservice.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;

import java.util.List;
import java.util.Map;

@Data
public class ListPileApiResponse {
    private boolean success;

    @JsonProperty("deck_id")
    private String deckId;

    private Map<String, Pile> piles;

    @Data
    public static class Pile {
        private List<CardDTO> cards;
        private int remaining;
    }
}
