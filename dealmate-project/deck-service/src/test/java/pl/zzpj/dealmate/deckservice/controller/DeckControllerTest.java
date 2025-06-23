package pl.zzpj.dealmate.deckservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.zzpj.dealmate.deckservice.dto.*;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckControllerTest {

    @Mock
    private DeckService deckService;

    @InjectMocks
    private DeckController deckController;

    private DeckEntity deckEntity;
    private PileEntity pileEntity;

    @BeforeEach
    void setUp() {
        deckEntity = new DeckEntity();
        deckEntity.setDeckId("testDeckId");
        deckEntity.setShuffled(true);
        deckEntity.setRemainingCards(52);

        pileEntity = new PileEntity();
        pileEntity.setPileName("testPile");
        pileEntity.setRemainingCards(3);
        pileEntity.setCardCodes(List.of("AS", "6H", "9D"));
    }

    // ================================
    // Testx for creating and reshuffling deck
    // ================================

    @Test
    void shouldCreateDeckSuccessfully() {
        // Test creating deck successfully
        when(deckService.createDeck(1)).thenReturn(deckEntity);

        ResponseEntity<DeckDTO> response = deckController.createDeck(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDeckId()).isEqualTo("testDeckId");
    }

    @Test
    void shouldReturnInternalServerErrorWhenCreatingDeck() {
        // Test error during deck creation
        when(deckService.createDeck(1)).thenThrow(new RuntimeException("Internal Server Error"));

        ResponseEntity<DeckDTO> response = deckController.createDeck(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldReshuffleDeckSuccessfully() {
        // Test reshuffling deck successfully
        when(deckService.shuffleDeck(1L)).thenReturn(deckEntity);

        ResponseEntity<DeckDTO> response = deckController.reshuffleDeck(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isShuffled()).isTrue();
    }

    @Test
    void shouldReturnInternalServerErrorWhenReshufflingDeck() {
        // Test error during deck reshuffle
        when(deckService.shuffleDeck(1L)).thenThrow(new RuntimeException("Deck not found"));

        ResponseEntity<DeckDTO> response = deckController.reshuffleDeck(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    // ================================
    // Tests for drawing cards from deck
    // ================================

    @Test
    void shouldReturnDrawnCards() {
        // Test successful draw of cards from deck
        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        CardDTO card2 = new CardDTO();
        card2.setCode("6H");

        List<CardDTO> cards = List.of(card1, card2);
        when(deckService.drawCardsFromDeck(1L, 2)).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.drawCardsFromDeck(1L, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawingCards() {
        // Test error when drawing cards from deck
        when(deckService.drawCardsFromDeck(1L, 2)).thenThrow(new RuntimeException("Failed"));

        ResponseEntity<List<CardDTO>> response = deckController.drawCardsFromDeck(1L, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    // ================================
    // Tests for adding and shuffling piles
    // ================================

    @Test
    void shouldAddCardsToPileSuccessfully() {
        // Test successfully adding cards to pile
        when(deckService.addCardsToPile(1L, "testPile", List.of("AS", "6H"))).thenReturn(pileEntity);

        ResponseEntity<PileDTO> response = deckController.addToPile(1L, "testPile", List.of("AS", "6H"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPileName()).isEqualTo("testPile");
    }

    @Test
    void shouldReturnInternalServerErrorWhenAddingCardsToPile() {
        // Test error when adding cards to pile
        when(deckService.addCardsToPile(1L, "testPile", List.of("AS"))).thenThrow(new RuntimeException("Error"));

        ResponseEntity<PileDTO> response = deckController.addToPile(1L, "testPile", List.of("AS"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldShufflePileSuccessfully() {
        // Test successful pile shuffle
        when(deckService.shufflePile(1L, "testPile")).thenReturn(pileEntity);

        ResponseEntity<PileDTO> response = deckController.shufflePile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenShufflingPile() {
        // Test error when shuffling pile
        when(deckService.shufflePile(1L, "testPile")).thenThrow(new RuntimeException("Shuffle error"));

        ResponseEntity<PileDTO> response = deckController.shufflePile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    // ================================
    // Tests for drawing cards from piles
    // ================================

    @Test
    void shouldListCardsInPile() {
        // Test listing cards from pile
        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        CardDTO card2 = new CardDTO();
        card2.setCode("2S");

        when(deckService.getCardsFromPile(1L, "testPile")).thenReturn(List.of(card1, card2));

        ResponseEntity<List<CardDTO>> response = deckController.listPileCards(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnInternalServerErrorWhenListPileCards() {
        // Test error when listing pile cards
        when(deckService.getCardsFromPile(1L, "testPile")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<CardDTO>> response = deckController.listPileCards(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldDrawSpecificCardsFromPile() {
        // Test drawing specific cards from pile
        CardDTO card = new CardDTO();
        card.setCode("AS");
        when(deckService.drawSpecificCardsFromPile(1L, "testPile", List.of("AS"))).thenReturn(List.of(card));

        ResponseEntity<List<CardDTO>> response = deckController.drawSpecificCardsFromPile(1L, "testPile", List.of("AS"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawSpecificCardsFromPile() {
        // Test error when drawing specific cards from pile
        when(deckService.drawSpecificCardsFromPile(1L, "testPile", List.of("AS"))).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<CardDTO>> response = deckController.drawSpecificCardsFromPile(1L, "testPile", List.of("AS"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldDrawCountFromPile() {
        // Test drawing count from pile
        CardDTO card = new CardDTO();
        card.setCode("KH");
        when(deckService.drawCountFromPile(1L, "testPile", 1)).thenReturn(List.of(card));

        ResponseEntity<List<CardDTO>> response = deckController.drawCountFromPile(1L, "testPile", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawCountFromPile() {
        // Test error when drawing count from pile
        when(deckService.drawCountFromPile(1L, "testPile", 1)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<CardDTO>> response = deckController.drawCountFromPile(1L, "testPile", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldDrawBottomFromPile() {
        // Test drawing bottom card from pile
        CardDTO card = new CardDTO();
        card.setCode("QD");
        when(deckService.drawBottomFromPile(1L, "testPile")).thenReturn(List.of(card));

        ResponseEntity<List<CardDTO>> response = deckController.drawBottomFromPile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawBottomFromPile() {
        // Test error when drawing bottom from pile
        when(deckService.drawBottomFromPile(1L, "testPile")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<CardDTO>> response = deckController.drawBottomFromPile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldDrawRandomFromPile() {
        // Test drawing random card from pile
        CardDTO cardDTO = new CardDTO();
        cardDTO.setCode("9C");
        when(deckService.drawRandomFromPile(1L, "testPile")).thenReturn(List.of(cardDTO));

        ResponseEntity<List<CardDTO>> response = deckController.drawRandomFromPile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawRandomFromPile() {
        // Test error when drawing random from pile
        when(deckService.drawRandomFromPile(1L, "testPile")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<CardDTO>> response = deckController.drawRandomFromPile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    // ================================
    // Tests for returning cards
    // ================================

    @Test
    void shouldReturnAllCardsToDeck() {
        // Test returning all cards to deck
        ResponseEntity<Void> response = deckController.returnAllToDeck(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnSpecificCardsToDeck() {
        // Test returning specific cards to deck
        ResponseEntity<Void> response = deckController.returnSpecificToDeck(1L, List.of("AS", "2S"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnAllCardsToPile() {
        // Test returning all cards to pile
        ResponseEntity<Void> response = deckController.returnAllToPile(1L, "testPile");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnSpecificCardsToPile() {
        // Test returning specific cards to pile
        ResponseEntity<Void> response = deckController.returnSpecificToPile(1L, "testPile", List.of("AS", "2S"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ================================
    // Verify service invocations for void methods
    // ================================

    @Test
    void shouldInvokeReturnAllToDeckService() {
        deckController.returnAllToDeck(1L);
        verify(deckService).returnAllCardsToDeck(1L);
    }

    @Test
    void shouldInvokeReturnSpecificToDeckService() {
        List<String> cards = List.of("AS", "2S");
        deckController.returnSpecificToDeck(1L, cards);
        verify(deckService).returnSpecificCardsToDeck(1L, cards);
    }

    @Test
    void shouldInvokeReturnAllToPileService() {
        deckController.returnAllToPile(1L, "testPile");
        verify(deckService).returnAllCardsToPile(1L, "testPile");
    }

    @Test
    void shouldInvokeReturnSpecificToPileService() {
        List<String> cards = List.of("AS", "2S");
        deckController.returnSpecificToPile(1L, "testPile", cards);
        verify(deckService).returnSpecificCardsToPile(1L, "testPile", cards);
    }
}
