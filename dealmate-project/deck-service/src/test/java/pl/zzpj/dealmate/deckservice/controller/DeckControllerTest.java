package pl.zzpj.dealmate.deckservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

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
        assertThat(response.getBody().getDeckId()).isEqualTo("testDeckId");
    }

    @Test
    void shouldReturnInternalServerError() {
        // Mockowanie błędnej odpowiedzi
        when(deckService.createDeck(1)).thenThrow(new RuntimeException("Internal Server Error"));

        // Wywołanie metody kontrolera
        ResponseEntity<DeckEntity> response = deckController.createDeck(1);

        // Sprawdzenie, czy odpowiedni kod błędu został zwrócony
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }
}