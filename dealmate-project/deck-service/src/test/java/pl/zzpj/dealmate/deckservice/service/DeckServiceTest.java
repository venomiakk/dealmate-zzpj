package pl.zzpj.dealmate.deckservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;

import pl.zzpj.dealmate.deckservice.exception.CreateDeckException;
import pl.zzpj.dealmate.deckservice.exception.DrawCardsException;
import pl.zzpj.dealmate.deckservice.exception.ShuffleDeckException;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;
import pl.zzpj.dealmate.deckservice.payload.response.*;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;
import pl.zzpj.dealmate.deckservice.repository.PileRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private PileRepository pileRepository;

    @InjectMocks
    private DeckService deckService;

    private final String deckApiUrl = "http://deck-api.com";

    @BeforeEach
    void setup() {
        deckService.deckApiUrl = deckApiUrl;
    }

    @Test
    void shouldCreateDeckSuccessfully() {
        CreateDeckApiResponse apiResponse = new CreateDeckApiResponse();
        apiResponse.setDeckId("deck123");
        apiResponse.setShuffled(true);
        apiResponse.setRemainingCards(52);

        when(restTemplate.getForObject(deckApiUrl + "/new/shuffle/?deck_count=1", CreateDeckApiResponse.class))
                .thenReturn(apiResponse);

        when(deckRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DeckEntity result = deckService.createDeck(1);

        assertThat(result.getDeckId()).isEqualTo("deck123");
        assertThat(result.isShuffled()).isTrue();
        assertThat(result.getRemainingCards()).isEqualTo(52);
    }

    @Test
    void shouldThrowCreateDeckExceptionWhenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(CreateDeckApiResponse.class))).thenReturn(null);
        assertThrows(CreateDeckException.class, () -> deckService.createDeck(1));
    }

    @Test
    void shouldShuffleDeckSuccessfully() {
        DeckEntity deck = new DeckEntity();
        deck.setId(1L);
        deck.setDeckId("deck123");

        ShuffleDeckApiResponse apiResponse = new ShuffleDeckApiResponse();
        apiResponse.setShuffled(true);
        apiResponse.setRemainingCards(40);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(deckApiUrl + "/deck123/shuffle/?remaining=true", ShuffleDeckApiResponse.class))
                .thenReturn(apiResponse);
        when(deckRepository.save(any())).thenReturn(deck);

        DeckEntity result = deckService.shuffleDeck(1L);

        assertThat(result.isShuffled()).isTrue();
        assertThat(result.getRemainingCards()).isEqualTo(40);
    }

    @Test
    void shouldThrowShuffleDeckExceptionWhenDeckNotFound() {
        when(deckRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ShuffleDeckException.class, () -> deckService.shuffleDeck(1L));
    }

    @Test
    void shouldThrowShuffleDeckExceptionWhenApiFails() {
        DeckEntity deck = new DeckEntity();
        deck.setId(1L);
        deck.setDeckId("deck123");
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(anyString(), eq(ShuffleDeckApiResponse.class))).thenReturn(null);
        assertThrows(ShuffleDeckException.class, () -> deckService.shuffleDeck(1L));
    }

    @Test
    void shouldDrawCardsFromDeckSuccessfully() {
        DeckEntity deck = new DeckEntity();
        deck.setDeckId("deck123");
        deck.setRemainingCards(10);
        deck.setDrawnCardCodes(new ArrayList<>());

        CardDTO card = new CardDTO();
        card.setCode("AS");

        DrawCardsApiResponse apiResponse = new DrawCardsApiResponse();
        apiResponse.setCards(List.of(card));
        apiResponse.setRemaining(9);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(anyString(), eq(DrawCardsApiResponse.class))).thenReturn(apiResponse);
        when(deckRepository.save(any())).thenReturn(deck);

        List<CardDTO> result = deckService.drawCardsFromDeck(1L, 1);
        assertThat(result).hasSize(1);
        assertThat(deck.getDrawnCardCodes()).contains("AS");
    }

    @Test
    void shouldThrowDrawCardsExceptionWhenNotEnoughCards() {
        DeckEntity deck = new DeckEntity();
        deck.setRemainingCards(0);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        assertThrows(DrawCardsException.class, () -> deckService.drawCardsFromDeck(1L, 1));
    }

    @Test
    void shouldThrowDrawCardsExceptionWhenApiFails() {
        DeckEntity deck = new DeckEntity();
        deck.setDeckId("deck123");
        deck.setRemainingCards(10);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(anyString(), eq(DrawCardsApiResponse.class))).thenReturn(null);
        assertThrows(DrawCardsException.class, () -> deckService.drawCardsFromDeck(1L, 1));
    }

    @Test
    void shouldAddCardsToPileSuccessfully() {
        DeckEntity deck = new DeckEntity();
        deck.setDeckId("deck123");
        deck.setDrawnCardCodes(new ArrayList<>(List.of("AS")));

        AddToPileApiResponse apiResponse = new AddToPileApiResponse();
        apiResponse.setSuccess(true);
        AddToPileApiResponse.PileDetails pileDetails = new AddToPileApiResponse.PileDetails();
        pileDetails.setRemaining(1);
        apiResponse.setPiles(Map.of("discardPile", pileDetails));

        PileEntity pile = new PileEntity();
        pile.setCardCodes(new ArrayList<>());

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(anyString(), eq(AddToPileApiResponse.class))).thenReturn(apiResponse);
        when(pileRepository.findByPileNameAndDeck("discardPile", deck)).thenReturn(Optional.of(pile));
        when(pileRepository.save(any())).thenReturn(pile);
        when(deckRepository.save(any())).thenReturn(deck);

        PileEntity result = deckService.addCardsToPile(1L, "discardPile", List.of("AS"));
        assertThat(result.getRemainingCards()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenCardNotDrawnWhileAddingToPile() {
        DeckEntity deck = new DeckEntity();
        deck.setDeckId("deck123");
        deck.setDrawnCardCodes(new ArrayList<>());

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        assertThrows(RuntimeException.class, () -> deckService.addCardsToPile(1L, "pile", List.of("AS")));
    }

    @Test
    void shouldShufflePileSuccessfully() {
        DeckEntity deck = new DeckEntity();
        deck.setDeckId("deck123");
        PileEntity pile = new PileEntity();

        ShufflePileApiResponse apiResponse = new ShufflePileApiResponse();
        apiResponse.setSuccess(true);
        ShufflePileApiResponse.PileInfo pileInfo = new ShufflePileApiResponse.PileInfo();
        pileInfo.setRemaining(3);
        apiResponse.setPiles(Map.of("pile1", pileInfo));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(anyString(), eq(ShufflePileApiResponse.class))).thenReturn(apiResponse);
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(pileRepository.save(any())).thenReturn(pile);

        PileEntity result = deckService.shufflePile(1L, "pile1");
        assertThat(result.getRemainingCards()).isEqualTo(3);
    }
}
