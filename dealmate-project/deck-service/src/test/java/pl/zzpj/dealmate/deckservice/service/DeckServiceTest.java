package pl.zzpj.dealmate.deckservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    void shouldGetCardsFromPileSuccessfully() {
        // Given
        String pileName = "player1_hand";
        CardDTO card = new CardDTO();
        card.setCode("AS");
        card.setSuit("SPADES");
        card.setValue("A");
        ListPileApiResponse.Pile pile = new ListPileApiResponse.Pile();
        pile.setCards(List.of(card));

        ListPileApiResponse apiResponse = new ListPileApiResponse();
        apiResponse.setSuccess(true);
        apiResponse.setPiles(Map.of(pileName, pile));

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(contains("/pile/" + pileName + "/list/"), eq(ListPileApiResponse.class)))
                .thenReturn(apiResponse);

        // When
        List<CardDTO> result = deckService.getCardsFromPile(1L, pileName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCode()).isEqualTo("AS");
    }

    @Test
    void shouldThrowExceptionWhenGettingCardsAndDeckNotFound() {
        // Given
        when(deckRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deckService.getCardsFromPile(99L, "any_pile"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Deck not found");
    }

    @Test
    void shouldDrawCardsFromPileSuccessfully() {
        // Given
        String pileName = "main_pile";
        int drawCount = 2;

        CardDTO card1 = new CardDTO();
        card1.setValue("K");
        card1.setSuit("HEARTS");
        card1.setCode("KH");
        CardDTO card2 = new CardDTO();
        card2.setValue("Q");
        card2.setSuit("DIAMONDS");
        card2.setCode("QD");
        DrawFromPileApiResponse apiResponse = new DrawFromPileApiResponse();
        apiResponse.setSuccess(true);
        apiResponse.setCards(List.of(card1, card2));
        apiResponse.setRemaining(50);

        PileEntity pileEntity = new PileEntity();
        pileEntity.setCardCodes(new ArrayList<>(List.of("KH", "QD", "JC")));
        pileEntity.setPileName(pileName);
        pileEntity.setDeck(deck);

        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(restTemplate.getForObject(contains("/draw/?count=" + drawCount), eq(DrawFromPileApiResponse.class)))
                .thenReturn(apiResponse);
        when(pileRepository.findByPileNameAndDeck(pileName, deck)).thenReturn(Optional.of(pileEntity));

        // When
        List<CardDTO> drawnCards = deckService.drawCountFromPile(1L, pileName, drawCount);

        // Then
        assertThat(drawnCards).hasSize(2);
        assertThat(drawnCards).extracting(CardDTO::getCode).containsExactlyInAnyOrder("KH", "QD");

        ArgumentCaptor<PileEntity> pileCaptor = ArgumentCaptor.forClass(PileEntity.class);
        verify(pileRepository).save(pileCaptor.capture());

        PileEntity savedPile = pileCaptor.getValue();
        assertThat(savedPile.getRemainingCards()).isEqualTo(50);
        assertThat(savedPile.getCardCodes()).hasSize(1).contains("JC"); // Sprawdź, czy usunięto wylosowane karty
    }

    @Test
    void shouldReturnAllCardsToDeckSuccessfully() {
        // Given
        PileEntity pile1 = new PileEntity();
        pile1.setDeck(deck);
        pile1.setCardCodes(new ArrayList<>(List.of("AH")));

        PileEntity pile2 = new PileEntity();
        pile2.setDeck(deck);
        pile2.setCardCodes(new ArrayList<>(List.of("KD")));

        deck.getDrawnCardCodes().add("2C");

        ReturnCardsApiResponse returnResponse = new ReturnCardsApiResponse();
        returnResponse.setSuccess(true);
        returnResponse.setRemaining(52);

        ListPileApiResponse listResponse = new ListPileApiResponse(); // Pusta odpowiedź, bo logika jej nie używa
        listResponse.setSuccess(true);
        listResponse.setPiles(Map.of());


        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(pileRepository.findAll()).thenReturn(List.of(pile1, pile2));
        // Mockowanie dwóch różnych wywołań restTemplate
        when(restTemplate.getForObject(contains("/return"), eq(ReturnCardsApiResponse.class))).thenReturn(returnResponse);
        when(restTemplate.getForObject(endsWith("/list/"), eq(ListPileApiResponse.class))).thenReturn(listResponse);

        // When
        deckService.returnAllCardsToDeck(1L);

        // Then
        // Weryfikacja, czy stosy zostały wyczyszczone i zapisane
        ArgumentCaptor<PileEntity> pileCaptor = ArgumentCaptor.forClass(PileEntity.class);
        verify(pileRepository, times(2)).save(pileCaptor.capture());

        List<PileEntity> savedPiles = pileCaptor.getAllValues();
        assertThat(savedPiles.get(0).getCardCodes()).isEmpty();
        assertThat(savedPiles.get(0).getRemainingCards()).isZero();
        assertThat(savedPiles.get(1).getCardCodes()).isEmpty();
        assertThat(savedPiles.get(1).getRemainingCards()).isZero();

        // Weryfikacja, czy talia została zaktualizowana i zapisana
        ArgumentCaptor<DeckEntity> deckCaptor = ArgumentCaptor.forClass(DeckEntity.class);
        verify(deckRepository).save(deckCaptor.capture());

        DeckEntity savedDeck = deckCaptor.getValue();
        assertThat(savedDeck.getDrawnCardCodes()).isEmpty();
        assertThat(savedDeck.getRemainingCards()).isEqualTo(52);
    }
}
