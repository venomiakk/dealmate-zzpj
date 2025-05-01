package pl.zzpj.dealmate.deckservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.external.response.CreateDeckApiResponse;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckService deckService;

    private CreateDeckApiResponse createDeckApiResponse;

    @BeforeEach
    void setUp() {
        createDeckApiResponse = new CreateDeckApiResponse();
        createDeckApiResponse.setDeckId("testDeckId");
        createDeckApiResponse.setShuffled(true);
        createDeckApiResponse.setRemainingCards(52);
    }

    @Test
    void shouldCreateDeckSuccessfully() {
        // Mockowanie odpowiedzi z API
        when(restTemplate.getForObject(anyString(), eq(CreateDeckApiResponse.class)))
                .thenReturn(createDeckApiResponse);

        // Wywołanie metody serwisowej
        DeckEntity deck = deckService.createDeck(1);

        // Sprawdzenie, czy odpowiedni obiekt został zapisany w repozytorium
        verify(deckRepository).save(any(DeckEntity.class));

        // Sprawdzenie, czy deck został poprawnie stworzony
        assertThat(deck).isNotNull();
        assertThat(deck.getDeckId()).isEqualTo("testDeckId");
        assertThat(deck.isShuffled()).isTrue();
        assertThat(deck.getRemainingCards()).isEqualTo(52);
    }

    @Test
    void shouldThrowExceptionWhenApiFails() {
        // Mockowanie błędnej odpowiedzi
        when(restTemplate.getForObject(anyString(), eq(CreateDeckApiResponse.class)))
                .thenReturn(null);

        // Testowanie rzucenia wyjątku
        assertThatThrownBy(() -> deckService.createDeck(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create a new deck");
    }
}