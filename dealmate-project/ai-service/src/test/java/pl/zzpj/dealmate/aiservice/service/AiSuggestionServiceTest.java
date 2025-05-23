package pl.zzpj.dealmate.aiservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zzpj.dealmate.aiservice.dto.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSuggestionServiceTest {

    @Mock
    private GroqClient aiClient;

    @InjectMocks
    private AiSuggestionService service;

    @Test
    void shouldReturnMoveForTexasHoldem() {
        // given
        PokerAiRequest request = new PokerAiRequest(
                List.of(),                      // deck
                List.of(),                      // discardPile
                List.of(new CardDto(Rank.ACE, Suit.SPADES),
                        new CardDto(Rank.ACE, Suit.HEARTS)),  // hand
                List.of(new CardDto(Rank.KING, Suit.CLUBS)),  // table
                PokerGameType.TEXAS_HOLDEM
        );

        when(aiClient.getAiMove(anyString())).thenReturn("RAISE");

        // when
        String move = service.getBestMove(request);

        // then
        assertThat(move).isEqualTo("RAISE");
        verify(aiClient).getAiMove(argThat(prompt ->
                prompt.contains("Game type: TEXAS_HOLDEM") &&
                        prompt.contains("ACE of SPADES")            // szybka kontrola formatu
        ));
    }

    @Test
    void shouldReturnMoveForFiveCardDraw() {
        // given
        PokerAiRequest request = new PokerAiRequest(
                List.of(),
                List.of(),
                List.of(new CardDto(Rank.TWO, Suit.HEARTS),
                        new CardDto(Rank.THREE, Suit.HEARTS),
                        new CardDto(Rank.FOUR, Suit.HEARTS),
                        new CardDto(Rank.FIVE, Suit.HEARTS),
                        new CardDto(Rank.SIX, Suit.HEARTS)),
                List.of(),
                PokerGameType.FIVE_CARD_DRAW
        );

        when(aiClient.getAiMove(anyString())).thenReturn("CALL");

        // when
        String move = service.getBestMove(request);

        // then
        assertThat(move).isEqualTo("CALL");
        verify(aiClient).getAiMove(argThat(prompt -> prompt.contains("FIVE_CARD_DRAW")));
    }
}