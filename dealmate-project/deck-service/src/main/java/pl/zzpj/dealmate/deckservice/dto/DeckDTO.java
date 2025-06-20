package pl.zzpj.dealmate.deckservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeckDTO {
    private Long id;
    private String deckId;
    private boolean shuffled;
    private int remainingCards;
    private List<String> drawnCardCodes;
}
