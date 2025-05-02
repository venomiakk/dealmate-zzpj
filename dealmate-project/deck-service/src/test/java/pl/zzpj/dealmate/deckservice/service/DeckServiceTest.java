package pl.zzpj.dealmate.deckservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.dto.ImageLinksDTO;
import pl.zzpj.dealmate.deckservice.payload.response.CreateDeckApiResponse;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.payload.response.DrawCardsApiResponse;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        ReflectionTestUtils.setField(deckService, "deckApiUrl", "https://deckofcardsapi.com/api/deck");

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

    @Test
    void shouldDrawCardsFromDeckSuccessfully() {
        // given
        long id = 1L;
        int count = 2;

        ImageLinksDTO imageLinksDTO = new ImageLinksDTO();
        imageLinksDTO.setPng("https://deckofcardsapi.com/static/img/AS.png");
        imageLinksDTO.setSvg("https://deckofcardsapi.com/static/img/AS.svg");

        DeckEntity deckEntity = new DeckEntity();
        deckEntity.setId(id);
        deckEntity.setDeckId("abc123");
        deckEntity.setRemainingCards(10);

        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        card1.setImages(imageLinksDTO);
        CardDTO card2 = new CardDTO();
        card2.setCode("6H");
        card2.setImages(imageLinksDTO);

        List<CardDTO> cards = new ArrayList<>();
        cards.add(card1);
        cards.add(card2);

        String expectedUrl = "https://deckofcardsapi.com/api/deck/abc123/draw/?count=2"; // przykład
        DrawCardsApiResponse response = new DrawCardsApiResponse();
        response.setSuccess(true);
        response.setDeck_id("abc123");
        response.setCards(cards);
        response.setRemaining(8);

        when(deckRepository.findById(id)).thenReturn(java.util.Optional.of(deckEntity));
        when(restTemplate.getForObject(expectedUrl, DrawCardsApiResponse.class)).thenReturn(response);

        // when
        List<CardDTO> drawnCards = deckService.drawCardsFromDeck(id, count);

        // then
        assertThat(drawnCards).hasSize(2);
        assertThat(drawnCards.getFirst().getCode()).isEqualTo("AS");
        assertThat(drawnCards.get(1).getCode()).isEqualTo("6H");
        verify(deckRepository).save(deckEntity);
        assertThat(deckEntity.getRemainingCards()).isEqualTo(8);
    }

    @Test
    void shouldThrowDeckNotFound(){
        when(deckRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> deckService.drawCardsFromDeck(1L, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Deck not found");
    }

    @Test
    void shouldThrowWhenRestReturnsNull(){
        long id = 1L;
        int count = 2;

        DeckEntity deckEntity = new DeckEntity();
        deckEntity.setId(id);
        deckEntity.setDeckId("abc123");
        deckEntity.setRemainingCards(10);

        when(deckRepository.findById(id)).thenReturn(java.util.Optional.of(deckEntity));
        when(restTemplate.getForObject(anyString(), eq(CardDTO[].class))).thenReturn(null);

        assertThatThrownBy(() -> deckService.drawCardsFromDeck(id, count))
                .isInstanceOf(RuntimeException.class);
    }
}