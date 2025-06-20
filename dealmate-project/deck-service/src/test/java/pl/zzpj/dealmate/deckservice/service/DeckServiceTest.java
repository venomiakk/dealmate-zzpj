package pl.zzpj.dealmate.deckservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;
import pl.zzpj.dealmate.deckservice.payload.response.*;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;
import pl.zzpj.dealmate.deckservice.repository.PileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private PileRepository pileRepository;

    @InjectMocks
    private DeckService deckService;

    private DeckEntity deck;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deckService, "deckApiUrl", "https://deckofcardsapi.com/api/deck");

        deck = new DeckEntity();
        deck.setId(1L);
        deck.setDeckId("abc123");
        deck.setRemainingCards(52);
    }

    @Test
    void shouldCreateDeckSuccessfully() {
        CreateDeckApiResponse apiResponse = new CreateDeckApiResponse();
        apiResponse.setDeckId("abc123");
        apiResponse.setShuffled(true);
        apiResponse.setRemainingCards(52);

        when(restTemplate.getForObject(anyString(), eq(CreateDeckApiResponse.class))).thenReturn(apiResponse);

        DeckEntity result = deckService.createDeck(1);

        verify(deckRepository).save(any());
        assertThat(result.getDeckId()).isEqualTo("abc123");
        assertThat(result.isShuffled()).isTrue();
    }

    @Test
    void shouldShuffleDeckSuccessfully() {
        ShuffleDeckApiResponse response = new ShuffleDeckApiResponse();
        response.setDeckId("abc123");
        response.setShuffled(true);
        response.setRemainingCards(52);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(contains("/shuffle/"), eq(ShuffleDeckApiResponse.class))).thenReturn(response);

        DeckEntity shuffledDeck = deckService.shuffleDeck(1L);

        verify(deckRepository).save(deck);
        assertThat(shuffledDeck.isShuffled()).isTrue();
    }

    @Test
    void shouldDrawCardsFromDeck() {
        DrawCardsApiResponse response = new DrawCardsApiResponse();
        CardDTO card = new CardDTO();
        card.setCode("AS");
        List<CardDTO> cards = List.of(card);
        response.setCards(cards);
        response.setRemaining(51);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(contains("/draw/?count=1"), eq(DrawCardsApiResponse.class))).thenReturn(response);

        List<CardDTO> result = deckService.drawCardsFromDeck(1L, 1);

        verify(deckRepository).save(deck);
        assertThat(result).hasSize(1);
        assertThat(deck.getDrawnCardCodes()).contains("AS");
    }

    @Test
    void shouldAddCardsToPile() {
        deck.setDrawnCardCodes(new ArrayList<>(List.of("AS", "2S")));

        AddToPileApiResponse response = new AddToPileApiResponse();
        AddToPileApiResponse.PileDetails pileDetails = new AddToPileApiResponse.PileDetails();
        pileDetails.setRemaining(2);
        response.setSuccess(true);
        response.setPiles(Map.of("pile1", pileDetails));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(contains("/add/"), eq(AddToPileApiResponse.class))).thenReturn(response);

        PileEntity pile = deckService.addCardsToPile(1L, "pile1", List.of("AS", "2S"));

        verify(pileRepository).save(any());
        assertThat(pile.getCardCodes()).contains("AS", "2S");
    }

    @Test
    void shouldShufflePile() {
        PileEntity pile = new PileEntity();
        pile.setPileName("pile1");
        pile.setCardCodes(new ArrayList<>(List.of("AS", "2S")));
        pile.setDeck(deck);

        ShufflePileApiResponse response = new ShufflePileApiResponse();
        ShufflePileApiResponse.PileInfo pileInfo = new ShufflePileApiResponse.PileInfo();
        pileInfo.setRemaining(2);
        response.setSuccess(true);
        response.setPiles(Map.of("pile1", pileInfo));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(restTemplate.getForObject(contains("/shuffle/"), eq(ShufflePileApiResponse.class))).thenReturn(response);

        PileEntity result = deckService.shufflePile(1L, "pile1");

        assertThat(result.getRemainingCards()).isEqualTo(2);
    }

    @Test void drawSpecificCardsFromPile() {
        CardDTO card = new CardDTO(); card.setCode("AS");
        DrawFromPileApiResponse response = new DrawFromPileApiResponse();
        response.setSuccess(true); response.setCards(List.of(card)); response.setRemaining(1);
        PileEntity pile = new PileEntity();
        pile.setDeck(deck); pile.setCardCodes(new ArrayList<>(List.of("AS")));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(restTemplate.getForObject(contains("/draw/?cards=AS"), eq(DrawFromPileApiResponse.class))).thenReturn(response);

        List<CardDTO> result = deckService.drawSpecificCardsFromPile(1L, "pile1", List.of("AS"));

        verify(pileRepository).save(pile);
        assertThat(result).hasSize(1);
        assertThat(pile.getCardCodes()).doesNotContain("AS");
    }

    @Test void drawBottomFromPile() {
        CardDTO card = new CardDTO(); card.setCode("KH");
        DrawFromPileApiResponse response = new DrawFromPileApiResponse();
        response.setSuccess(true); response.setCards(List.of(card)); response.setRemaining(0);
        PileEntity pile = new PileEntity();
        pile.setDeck(deck); pile.setCardCodes(new ArrayList<>(List.of("KH")));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(restTemplate.getForObject(contains("/draw/bottom/"), eq(DrawFromPileApiResponse.class))).thenReturn(response);

        List<CardDTO> result = deckService.drawBottomFromPile(1L, "pile1");

        verify(pileRepository).save(pile);
        assertThat(pile.getCardCodes()).doesNotContain("KH");
    }

    @Test void drawRandomFromPile() {
        CardDTO card = new CardDTO(); card.setCode("7D");
        DrawFromPileApiResponse response = new DrawFromPileApiResponse();
        response.setSuccess(true); response.setCards(List.of(card)); response.setRemaining(0);
        PileEntity pile = new PileEntity();
        pile.setDeck(deck); pile.setCardCodes(new ArrayList<>(List.of("7D")));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(restTemplate.getForObject(contains("/draw/random/"), eq(DrawFromPileApiResponse.class))).thenReturn(response);

        List<CardDTO> result = deckService.drawRandomFromPile(1L, "pile1");

        verify(pileRepository).save(pile);
        assertThat(pile.getCardCodes()).doesNotContain("7D");
    }

    @Test void returnSpecificCardsToDeck() {
        deck.setDrawnCardCodes(new ArrayList<>(List.of("AS", "2S")));
        ReturnCardsApiResponse response = new ReturnCardsApiResponse();
        response.setSuccess(true); response.setDeckId("abc123"); response.setRemaining(50);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(contains("/return/?cards=AS,2S"), eq(ReturnCardsApiResponse.class)))
                .thenReturn(response);

        deckService.returnSpecificCardsToDeck(1L, List.of("AS", "2S"));

        verify(deckRepository).save(deck);
        assertThat(deck.getDrawnCardCodes()).isEmpty();
    }

    @Test void returnAllCardsToPile() {
        PileEntity pile = new PileEntity();
        pile.setDeck(deck); pile.setPileName("pile1"); pile.setCardCodes(new ArrayList<>(List.of("3H", "5C")));
        ReturnCardsApiResponse response = new ReturnCardsApiResponse();
        response.setSuccess(true); response.setRemaining(2);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(restTemplate.getForObject(contains("/pile/pile1/return/"), eq(ReturnCardsApiResponse.class)))
                .thenReturn(response);

        deckService.returnAllCardsToPile(1L, "pile1");

        verify(pileRepository).save(pile);
        assertThat(pile.getRemainingCards()).isEqualTo(2);
    }

    @Test void returnSpecificCardsToPile() {
        PileEntity pile = new PileEntity();
        pile.setDeck(deck); pile.setPileName("pile1"); pile.setCardCodes(new ArrayList<>(List.of("3H")));
        ReturnCardsApiResponse response = new ReturnCardsApiResponse();
        response.setSuccess(true); response.setRemaining(3);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findByPileNameAndDeck("pile1", deck)).thenReturn(Optional.of(pile));
        when(restTemplate.getForObject(contains("/pile/pile1/return/?cards=AS,2S"), eq(ReturnCardsApiResponse.class)))
                .thenReturn(response);

        deckService.returnSpecificCardsToPile(1L, "pile1", List.of("AS", "2S"));

        verify(pileRepository).save(pile);
        assertThat(pile.getCardCodes()).contains("AS", "2S", "3H");
    }

    @Test
    void shouldThrowIfDeckNotFound() {
        when(deckRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.drawCardsFromDeck(99L, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Deck not found");
    }
}
