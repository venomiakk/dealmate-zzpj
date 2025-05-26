package pl.zzpj.dealmate.deckservice.payload.response;

import lombok.Data;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;

import java.util.Map;
import java.util.List;

@Data
public class ShufflePileApiResponse {
    private boolean success;
    private String deck_id;
    private int remaining;
    private Map<String, PileInfo> piles;
    private List<CardDTO> cards;

    @Data
    public static class PileInfo {
        private int remaining;
    }
}
