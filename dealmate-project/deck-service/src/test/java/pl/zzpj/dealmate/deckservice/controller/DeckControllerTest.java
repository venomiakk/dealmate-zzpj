package pl.zzpj.dealmate.deckservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.dto.ImageLinksDTO;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeckControllerTest {

    @Mock
    private DeckService deckService;

    @InjectMocks
    private DeckController deckController;

    private DeckEntity deckEntity;

    @BeforeEach
    void setUp() {
        deckEntity = new DeckEntity();
        deckEntity.setDeckId("testDeckId");
        deckEntity.setShuffled(true);
        deckEntity.setRemainingCards(52);
    }

    @Test
    void shouldCreateDeckSuccessfully() {
        // Mockowanie odpowiedzi z serwisu
        when(deckService.createDeck(1)).thenReturn(deckEntity);

        // Wywołanie metody kontrolera
        ResponseEntity<DeckEntity> response = deckController.createDeck(1);

        // Sprawdzenie, czy odpowiedni obiekt został zwrócony
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(deckEntity);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDeckId()).isEqualTo("testDeckId");
    }

    @Test
    void shouldReturnInternalServerErrorWhenCreatingDeck() {
        // Mockowanie błędnej odpowiedzi
        when(deckService.createDeck(1)).thenThrow(new RuntimeException("Internal Server Error"));

        // Wywołanie metody kontrolera
        ResponseEntity<DeckEntity> response = deckController.createDeck(1);

        // Sprawdzenie, czy odpowiedni kod błędu został zwrócony
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldReturnDrawnCards(){
        ImageLinksDTO imageLinksDTO = new ImageLinksDTO();
        imageLinksDTO.setPng("https://deckofcardsapi.com/AS.png");
        imageLinksDTO.setSvg("https://deckofcardsapi.com/AS.svg");
        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        card1.setImages(imageLinksDTO);
        CardDTO card2 = new CardDTO();
        card2.setCode("6H");
        card2.setImages(imageLinksDTO);

        List<CardDTO> cards = new ArrayList<>();
        cards.add(card1);
        cards.add(card2);
        // Mockowanie odpowiedzi z serwisu
        when(deckService.drawCardsFromDeck(1L, 2)).thenReturn(cards);

        // Wywołanie metody kontrolera
        ResponseEntity<List<CardDTO>> response = deckController.drawCardsFromDeck(1L, 2);

        // Sprawdzenie, czy odpowiedni obiekt został zwrócony
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().getFirst().getCode()).isEqualTo("AS");
    }

    @Test
    void shouldReturnInternalServerErrorWhenDrawingCards() {
        // Mockowanie błędnej odpowiedzi
        when(deckService.drawCardsFromDeck(1L, 2)).thenThrow(new RuntimeException("Internal Server Error"));

        // Wywołanie metody kontrolera
        ResponseEntity<List<CardDTO>> response = deckController.drawCardsFromDeck(1L, 2);

        // Sprawdzenie, czy odpowiedni kod błędu został zwrócony
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }
}