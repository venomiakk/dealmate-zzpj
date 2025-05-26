package pl.zzpj.dealmate.deckservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class PileDTO {
    private String pileName;
    private int remainingCards;
    private List<String> cardCodes;
}
