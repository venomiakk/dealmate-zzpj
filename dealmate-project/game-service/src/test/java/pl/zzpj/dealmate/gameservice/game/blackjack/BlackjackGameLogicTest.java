package pl.zzpj.dealmate.gameservice.game.blackjack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.game.dto.CardDto;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BlackjackGameLogicTest {

    @Mock private GameRoom mockRoom;
    @Mock private DeckServiceClient mockDeckService;
    @Mock private SimpMessagingTemplate mockMessagingTemplate;
    @Mock private GameHistoryService mockGameHistoryService;

    private BlackjackGame blackjackGame;

    // Helper do tworzenia kart
    private CardDto card(String value) {
        // Suit i Code nie mają znaczenia dla liczenia wartości, więc mogą być uproszczone
        return new CardDto(value, value, "S", null);
    }

    private CardDto ace() {
        return card("ACE");
    }

    @BeforeEach
    void setUp() {
        // Tworzymy instancję gry dla testów; lista graczy jest pusta, bo testujemy logikę wewnętrzną
        blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("player1"), mockGameHistoryService);
    }

    // Używamy refleksji, aby uzyskać dostęp do prywatnej metody
    private int invokeCalculateHandValue(List<CardDto> hand) throws Exception {
        Method method = BlackjackGame.class.getDeclaredMethod("calculateHandValue", List.class);
        method.setAccessible(true);
        return (int) method.invoke(blackjackGame, hand);
    }

    @Test
    void calculateHandValue_shouldSumSimpleCardsCorrectly() throws Exception {
        // Given
        List<CardDto> hand = List.of(card("7"), card("9")); // 7 + 9 = 16

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(16);
    }

    @Test
    void calculateHandValue_shouldCountFigureAsTen() throws Exception {
        // Given
        List<CardDto> hand = List.of(card("KING"), card("3")); // 10 + 3 = 13

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(13);
    }

    @Test
    void calculateHandValue_shouldTreatAceAsElevenWhenNotBusting() throws Exception {
        // Given
        List<CardDto> hand = List.of(ace(), card("8")); // 11 + 8 = 19

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(19);
    }

    @Test
    void calculateHandValue_shouldTreatAceAsOneWhenBusting() throws Exception {
        // Given
        List<CardDto> hand = List.of(ace(), card("8"), card("5")); // 11 + 8 + 5 = 24 -> 1 + 8 + 5 = 14

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(14);
    }

    @Test
    void calculateHandValue_shouldHandleMultipleAcesCorrectly() throws Exception {
        // Given
        List<CardDto> hand = List.of(ace(), ace(), card("9")); // 11 + 11 + 9 = 31 -> 1 + 11 + 9 = 21

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(21);
    }

    @Test
    void calculateHandValue_shouldHandleFourAces() throws Exception {
        // Given
        List<CardDto> hand = List.of(ace(), ace(), ace(), ace()); // 11+1+1+1 = 14

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(14);
    }

    @Test
    void calculateHandValue_shouldReturnCorrectValueForBlackjack() throws Exception {
        // Given
        List<CardDto> hand = List.of(ace(), card("JACK")); // 11 + 10 = 21

        // When
        int value = invokeCalculateHandValue(hand);

        // Then
        assertThat(value).isEqualTo(21);
    }
}
