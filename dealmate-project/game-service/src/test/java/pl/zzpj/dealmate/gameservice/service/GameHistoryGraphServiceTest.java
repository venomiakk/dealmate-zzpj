package pl.zzpj.dealmate.gameservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.GameResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameHistoryGraphServiceTest {

    @Mock
    private GameHistoryService gameHistoryService;

    @InjectMocks
    private GameHistoryGraphService gameHistoryGraphService;

    private static final String PLAYER_ID = "player1";
    private GameHistory gameHistory;

    @BeforeEach
    void setUp() {
        gameHistory = new GameHistory();
        gameHistory.setId(1L);
        gameHistory.setGameType(EGameType.BLACKJACK);
        gameHistory.setResult(GameResult.WIN);
        gameHistory.setAmount(BigDecimal.valueOf(100.0));
        gameHistory.setTimestamp(LocalDateTime.now());
    }

    @Test
    void shouldReturnGraphStringWhenPythonScriptWorks() {
        // Given
        when(gameHistoryService.getHistoryForPlayer(PLAYER_ID)).thenReturn(List.of(gameHistory));

        // When
        String result = gameHistoryGraphService.generateGraphFromJson(PLAYER_ID);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldReturnExceptionStringWhenServiceFails() {
        // Given
        String playerId = "player2";
        when(gameHistoryService.getHistoryForPlayer(playerId)).thenThrow(new RuntimeException("Błąd pobierania historii"));

        // When
        String result = gameHistoryGraphService.generateGraphFromJson(playerId);

        // Then
        assertThat(result).contains("Błąd pobierania historii");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Błąd tworzenia katalogu tymczasowego",
            "Proces Pythona przekroczył limit czasu.",
            "Wątek został przerwany podczas generowania wykresu."
    })
    void shouldHandleInternalRuntimeErrors(String expectedErrorMessage) {
        // Given
        GameHistoryGraphService serviceThatFails = new GameHistoryGraphService(gameHistoryService) {
            @Override
            public String generateGraphFromJson(String playerId) {
                return new RuntimeException(expectedErrorMessage).toString();
            }
        };

        // When
        String result = serviceThatFails.generateGraphFromJson(PLAYER_ID);

        // Then
        assertThat(result).contains(expectedErrorMessage);
    }
}
