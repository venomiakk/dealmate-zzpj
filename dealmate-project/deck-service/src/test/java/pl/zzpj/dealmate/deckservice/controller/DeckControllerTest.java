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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void shouldCreateDeckSuccessfully() {
        when(deckService.createDeck(1)).thenReturn(deckEntity);

        ResponseEntity<DeckDTO> response = deckController.createDeck(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDeckId()).isEqualTo("testDeckId");
    }

    @Test
    void shouldReturnInternalServerErrorWhenCreatingDeck() {
        when(deckService.createDeck(1)).thenThrow(new RuntimeException("Internal Server Error"));

        ResponseEntity<DeckDTO> response = deckController.createDeck(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldReshuffleDeckSuccessfully() {
        when(deckService.shuffleDeck(1L)).thenReturn(deckEntity);

        ResponseEntity<DeckDTO> response = deckController.reshuffleDeck(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isShuffled()).isTrue();
    }

    @Test
    void shouldReturnInternalServerErrorWhenReshufflingDeck() {
        when(deckService.shuffleDeck(1L)).thenThrow(new RuntimeException("Deck not found"));

        ResponseEntity<DeckDTO> response = deckController.reshuffleDeck(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldReturnDrawnCards() {
        ImageLinksDTO imageLinksDTO = new ImageLinksDTO();
        imageLinksDTO.setPng("https://deckofcardsapi.com/AS.png");
        imageLinksDTO.setSvg("https://deckofcardsapi.com/AS.svg");

        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        card1.setImages(imageLinksDTO);

        CardDTO card2 = new CardDTO();
        card2.setCode("6H");
        card2.setImages(imageLinksDTO);

        List<CardDTO> cards = List.of(card1, card2);

        when(deckService.drawCardsFromDeck(1L, 2)).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.drawCardsFromDeck(1L, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().getFirst().getCode()).isEqualTo("AS");
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawingCards() {
        when(deckService.drawCardsFromDeck(1L, 2)).thenThrow(new RuntimeException("Failed"));

        ResponseEntity<List<CardDTO>> response = deckController.drawCardsFromDeck(1L, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldAddCardsToPileSuccessfully() {
        when(deckService.addCardsToPile(1L, "testPile", List.of("AS", "6H"))).thenReturn(pileEntity);

        ResponseEntity<PileDTO> response = deckController.addToPile(1L, "testPile", List.of("AS", "6H"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPileName()).isEqualTo("testPile");
        assertThat(response.getBody().getCardCodes()).contains("AS", "6H", "9D");
    }

    @Test
    void shouldReturnInternalServerErrorWhenAddingCardsToPile() {
        when(deckService.addCardsToPile(1L, "testPile", List.of("AS"))).thenThrow(new RuntimeException("Error"));

        ResponseEntity<PileDTO> response = deckController.addToPile(1L, "testPile", List.of("AS"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldShufflePileSuccessfully() {
        when(deckService.shufflePile(1L, "testPile")).thenReturn(pileEntity);

        ResponseEntity<PileDTO> response = deckController.shufflePile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPileName()).isEqualTo("testPile");
        assertThat(response.getBody().getRemainingCards()).isEqualTo(3);
    }

    @Test
    void shouldReturnInternalServerErrorWhenShufflingPile() {
        when(deckService.shufflePile(1L, "testPile")).thenThrow(new RuntimeException("Shuffle error"));

        ResponseEntity<PileDTO> response = deckController.shufflePile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }


    @Test
    void shouldListCardsInPile() {
        List<CardDTO> cards = List.of(new CardDTO() {{ setCode("AS"); }}, new CardDTO() {{ setCode("2S"); }});
        when(deckService.getCardsFromPile(1L, "testPile")).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.listPileCards(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldDrawSpecificCardsFromPile() {
        List<CardDTO> cards = List.of(new CardDTO() {{ setCode("AS"); }});
        when(deckService.drawSpecificCardsFromPile(1L, "testPile", List.of("AS"))).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.drawSpecificCardsFromPile(1L, "testPile", List.of("AS"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldDrawCountFromPile() {
        List<CardDTO> cards = List.of(new CardDTO() {{ setCode("KH"); }});
        when(deckService.drawCountFromPile(1L, "testPile", 1)).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.drawCountFromPile(1L, "testPile", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldDrawBottomFromPile() {
        List<CardDTO> cards = List.of(new CardDTO() {{ setCode("QD"); }});
        when(deckService.drawBottomFromPile(1L, "testPile")).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.drawBottomFromPile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldDrawRandomFromPile() {
        List<CardDTO> cards = List.of(new CardDTO() {{ setCode("9C"); }});
        when(deckService.drawRandomFromPile(1L, "testPile")).thenReturn(cards);

        ResponseEntity<List<CardDTO>> response = deckController.drawRandomFromPile(1L, "testPile");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldReturnAllCardsToDeck() {
        ResponseEntity<Void> response = deckController.returnAllToDeck(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnSpecificCardsToDeck() {
        ResponseEntity<Void> response = deckController.returnSpecificToDeck(1L, List.of("AS", "2S"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnAllCardsToPile() {
        ResponseEntity<Void> response = deckController.returnAllToPile(1L, "testPile");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnSpecificCardsToPile() {
        ResponseEntity<Void> response = deckController.returnSpecificToPile(1L, "testPile", List.of("AS", "2S"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
