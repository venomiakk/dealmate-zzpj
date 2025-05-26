package pl.zzpj.dealmate.deckservice.mapper;

import pl.zzpj.dealmate.deckservice.dto.DeckDTO;
import pl.zzpj.dealmate.deckservice.dto.PileDTO;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;

public class EntityToDtoMapper {

    public static DeckDTO toDeckDTO(DeckEntity entity) {
        DeckDTO dto = new DeckDTO();
        dto.setDeckId(entity.getDeckId());
        dto.setShuffled(entity.isShuffled());
        dto.setRemainingCards(entity.getRemainingCards());
        dto.setDrawnCardCodes(entity.getDrawnCardCodes());
        return dto;
    }

    public static PileDTO toPileDTO(PileEntity entity) {
        PileDTO dto = new PileDTO();
        dto.setPileName(entity.getPileName());
        dto.setRemainingCards(entity.getRemainingCards());
        dto.setCardCodes(entity.getCardCodes());
        return dto;
    }
}
