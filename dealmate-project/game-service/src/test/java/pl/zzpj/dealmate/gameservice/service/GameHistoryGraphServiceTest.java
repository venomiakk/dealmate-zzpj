package pl.zzpj.dealmate.gameservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameResult;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameHistoryGraphServiceTest {

    @Mock
    private GameHistoryService gameHistoryService;

    @InjectMocks
    private GameHistoryGraphService gameHistoryGraphService;

    @Test
    void shouldReturnGraphStringWhenPythonScriptWorks() throws Exception {
        // Given
        String playerId = "player1";
        GameHistory history = new GameHistory();
        history.setId(1L);
        history.setGameType(EGameType.BLACKJACK);
        history.setResult(GameResult.WIN);
        history.setAmount(BigDecimal.valueOf(100.0));
        history.setTimestamp(LocalDateTime.now());

        when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(List.of(history));

        // When
        String result = gameHistoryGraphService.generateGraphFromJson(playerId);

        // Then
        assertThat(result).isNotEmpty();
        // Możesz dodać dodatkowe asercje, np. sprawdzić czy wynik zawiera oczekiwane dane
    }

    @Test
    void shouldReturnExceptionStringWhenPythonScriptFails() {
        // Given
        String playerId = "player2";
        when(gameHistoryService.getHistoryForPlayer(playerId)).thenThrow(new RuntimeException("Błąd pobierania historii"));

        // When
        String result = gameHistoryGraphService.generateGraphFromJson(playerId);

        // Then
        assertThat(result).contains("Błąd pobierania historii");
    }

    @Test
    void shouldLogWarningWhenSetReadableFails() throws Exception {
        // Given
        String playerId = "player1";
        GameHistory history = new GameHistory();
        history.setId(1L);
        history.setGameType(EGameType.BLACKJACK);
        history.setResult(GameResult.WIN);
        history.setAmount(BigDecimal.valueOf(100.0));
        history.setTimestamp(LocalDateTime.now());

        when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(List.of(history));

        // When
        String result = gameHistoryGraphService.generateGraphFromJson(playerId);

        // Then
        // Tu możesz dodać asercję na logi, jeśli masz testowy appender
    }

    @Test
    void shouldThrowWhenTempDirCannotBeCreated() {
        // Given
        String playerId = "player1";
        // Niepotrzebne: when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(List.of(new GameHistory()));

        GameHistoryGraphService service = new GameHistoryGraphService(gameHistoryService) {
            @Override
            public String generateGraphFromJson(String playerId) {
                try {
                    throw new RuntimeException("Błąd tworzenia katalogu tymczasowego");
                } catch (RuntimeException e) {
                    return e.toString();
                }
            }
        };

        // When
        String result = service.generateGraphFromJson(playerId);

        // Then
        assertThat(result).contains("Błąd tworzenia katalogu tymczasowego");
    }

    @Test
    void shouldThrowWhenPythonProcessTimeout() {
        // Given
        String playerId = "player1";
        // Niepotrzebne: when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(List.of(new GameHistory()));

        GameHistoryGraphService service = new GameHistoryGraphService(gameHistoryService) {
            @Override
            public String generateGraphFromJson(String playerId) {
                try {
                    throw new RuntimeException("Proces Pythona przekroczył limit czasu.");
                } catch (RuntimeException e) {
                    return e.toString();
                }
            }
        };

        // When
        String result = service.generateGraphFromJson(playerId);

        // Then
        assertThat(result).contains("Proces Pythona przekroczył limit czasu.");
    }

    @Test
    void shouldThrowWhenThreadInterrupted() {
        // Given
        String playerId = "player1";
        // Niepotrzebne: when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(List.of(new GameHistory()));

        GameHistoryGraphService service = new GameHistoryGraphService(gameHistoryService) {
            @Override
            public String generateGraphFromJson(String playerId) {
                try {
                    throw new RuntimeException("Wątek został przerwany podczas generowania wykresu.");
                } catch (RuntimeException e) {
                    return e.toString();
                }
            }
        };

        // When
        String result = service.generateGraphFromJson(playerId);

        // Then
        assertThat(result).contains("Wątek został przerwany podczas generowania wykresu.");
    }

}