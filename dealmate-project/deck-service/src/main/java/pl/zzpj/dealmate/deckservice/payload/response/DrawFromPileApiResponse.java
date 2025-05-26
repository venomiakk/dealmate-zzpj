package pl.zzpj.dealmate.deckservice.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;

import java.util.List;

@Data
public class DrawFromPileApiResponse {
    private boolean success;

    @JsonProperty("deck_id")
    private String deckId;

    private List<CardDTO> cards;

    private int remaining;
}
