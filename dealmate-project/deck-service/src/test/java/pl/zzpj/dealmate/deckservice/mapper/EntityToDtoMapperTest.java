package pl.zzpj.dealmate.deckservice.mapper;

import org.junit.jupiter.api.Test;
import pl.zzpj.dealmate.deckservice.dto.DeckDTO;
import pl.zzpj.dealmate.deckservice.dto.PileDTO;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntityToDtoMapperTest {

    @Test
    void shouldMapDeckEntityToDeckDTO() {

        DeckEntity entity = new DeckEntity();
        entity.setId(1L);
        entity.setDeckId("deck123");
        entity.setShuffled(true);
        entity.setRemainingCards(52);
        entity.setDrawnCardCodes(List.of("AS", "KH", "3D"));


        DeckDTO dto = EntityToDtoMapper.toDeckDTO(entity);


        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDeckId()).isEqualTo("deck123");
        assertThat(dto.isShuffled()).isTrue();
        assertThat(dto.getRemainingCards()).isEqualTo(52);
        assertThat(dto.getDrawnCardCodes()).containsExactly("AS", "KH", "3D");
    }

    @Test
    void shouldMapDeckEntityWithNullDrawnCards() {

        DeckEntity entity = new DeckEntity();
        entity.setId(2L);
        entity.setDeckId("deck456");
        entity.setShuffled(false);
        entity.setRemainingCards(20);
        entity.setDrawnCardCodes(null);


        DeckDTO dto = EntityToDtoMapper.toDeckDTO(entity);


        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getDeckId()).isEqualTo("deck456");
        assertThat(dto.isShuffled()).isFalse();
        assertThat(dto.getRemainingCards()).isEqualTo(20);
        assertThat(dto.getDrawnCardCodes()).isNull();
    }

    @Test
    void shouldMapPileEntityToPileDTO() {
        PileEntity entity = new PileEntity();
        entity.setPileName("discardPile");
        entity.setRemainingCards(10);
        entity.setCardCodes(List.of("QD", "7H", "2C"));

        PileDTO dto = EntityToDtoMapper.toPileDTO(entity);

        assertThat(dto.getPileName()).isEqualTo("discardPile");
        assertThat(dto.getRemainingCards()).isEqualTo(10);
        assertThat(dto.getCardCodes()).containsExactly("QD", "7H", "2C");
    }

    @Test
    void shouldMapPileEntityWithNullCardCodes() {
        PileEntity entity = new PileEntity();
        entity.setPileName("pileA");
        entity.setRemainingCards(5);
        entity.setCardCodes(null);


        PileDTO dto = EntityToDtoMapper.toPileDTO(entity);


        assertThat(dto.getPileName()).isEqualTo("pileA");
        assertThat(dto.getRemainingCards()).isEqualTo(5);
        assertThat(dto.getCardCodes()).isNull();
    }
}
