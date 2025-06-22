package pl.zzpj.dealmate.gameservice.client;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.zzpj.dealmate.gameservice.game.dto.CardDto;
import pl.zzpj.dealmate.gameservice.game.dto.DeckDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckServiceClientTest {

    @Test
    void testCreateDeck() {
        DeckServiceClient deckServiceClient = Mockito.mock(DeckServiceClient.class);
        DeckDto deckDto = new DeckDto("deck123", true, 52, 1L);

        Mockito.when(deckServiceClient.createDeck(1)).thenReturn(deckDto);

        DeckDto result = deckServiceClient.createDeck(1);
        assertEquals(deckDto, result);
    }

    @Test
    void testDrawCards() {
        DeckServiceClient deckServiceClient = Mockito.mock(DeckServiceClient.class);
        CardDto cardDto = new CardDto("AS", "ACE", "SPADES", null);
        Mockito.when(deckServiceClient.drawCards(1L, 1)).thenReturn(List.of(cardDto));

        List<CardDto> result = deckServiceClient.drawCards(1L, 1);
        assertEquals(1, result.size());
        assertEquals(cardDto, result.get(0));
    }
}
