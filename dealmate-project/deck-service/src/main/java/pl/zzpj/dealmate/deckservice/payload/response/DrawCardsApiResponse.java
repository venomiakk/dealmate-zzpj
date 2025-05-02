package pl.zzpj.dealmate.deckservice.payload.response;

import lombok.Data;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;

import java.util.List;

@Data
public class DrawCardsApiResponse {
    private boolean success;
    private String deck_id;
    private List<CardDTO> cards;
    private int remaining;
}
