package pl.zzpj.dealmate.gameservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.repository.GameHistoryRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameHistoryServiceTest {

    @Mock
    private GameHistoryRepository gameHistoryRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private GameHistoryService gameHistoryService;

    @Test
    void recordGameResults_shouldSaveHistoryAndAddCreditsOnWin() {
        // Given
        String playerId = "winner1";
        GameResult result = GameResult.WIN;
        BigDecimal entryFee = new BigDecimal("100");

        ArgumentCaptor<GameHistory> historyCaptor = ArgumentCaptor.forClass(GameHistory.class);

        // When
        gameHistoryService.recordGameResults(playerId, result, entryFee);

        // Then
        // 1. Sprawdź, czy historia została poprawnie zapisana do repozytorium
        verify(gameHistoryRepository).save(historyCaptor.capture());
        GameHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getPlayerId()).isEqualTo(playerId);
        assertThat(savedHistory.getResult()).isEqualTo(result);
        assertThat(savedHistory.getAmount()).isEqualByComparingTo("100");

        // 2. Sprawdź, czy kredyty zostały poprawnie zaktualizowane
        verify(userServiceClient).updateUserCredits(eq(playerId), eq(100L));
    }

    @Test
    void recordGameResults_shouldSaveHistoryAndSubtractCreditsOnLoss() {
        // Given
        String playerId = "loser1";
        GameResult result = GameResult.LOSS;
        BigDecimal entryFee = new BigDecimal("50");

        ArgumentCaptor<GameHistory> historyCaptor = ArgumentCaptor.forClass(GameHistory.class);

        // When
        gameHistoryService.recordGameResults(playerId, result, entryFee);

        // Then
        // 1. Sprawdź historię
        verify(gameHistoryRepository).save(historyCaptor.capture());
        GameHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getPlayerId()).isEqualTo(playerId);
        assertThat(savedHistory.getResult()).isEqualTo(result);
        assertThat(savedHistory.getAmount()).isEqualByComparingTo("-50"); // Ujemna kwota

        // 2. Sprawdź aktualizację kredytów (z ujemną wartością)
        verify(userServiceClient).updateUserCredits(eq(playerId), eq(-50L));
    }

    @Test
    void recordGameResults_shouldSaveHistoryAndAddBonusCreditsOnBlackjackWin() {
        // Given
        String playerId = "luckyPlayer";
        GameResult result = GameResult.BLACKJACK_WIN;
        BigDecimal entryFee = new BigDecimal("100");
        BigDecimal expectedWin = new BigDecimal("150.0"); // 100 * 1.5

        ArgumentCaptor<GameHistory> historyCaptor = ArgumentCaptor.forClass(GameHistory.class);

        // When
        gameHistoryService.recordGameResults(playerId, result, entryFee);

        // Then
        // 1. Sprawdź historię
        verify(gameHistoryRepository).save(historyCaptor.capture());
        GameHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getResult()).isEqualTo(result);
        assertThat(savedHistory.getAmount()).isEqualByComparingTo("150.0");

        // 2. Sprawdź aktualizację kredytów
        verify(userServiceClient).updateUserCredits(eq(playerId), eq(150L));
    }

    @Test
    void recordGameResults_shouldSaveHistoryButNotChangeCreditsOnPush() {
        // Given
        String playerId = "player1";
        GameResult result = GameResult.PUSH;
        BigDecimal entryFee = new BigDecimal("200");

        ArgumentCaptor<GameHistory> historyCaptor = ArgumentCaptor.forClass(GameHistory.class);

        // When
        gameHistoryService.recordGameResults(playerId, result, entryFee);

        // Then
        // 1. Sprawdź historię (z kwotą 0)
        verify(gameHistoryRepository).save(historyCaptor.capture());
        GameHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getResult()).isEqualTo(result);
        assertThat(savedHistory.getAmount()).isEqualByComparingTo("0");

        // 2. Sprawdź, czy updateUserCredits NIE zostało wywołane
        verify(userServiceClient, never()).updateUserCredits(anyString(), anyLong());
    }

    @Test
    void getHistoryForPlayer_shouldCallRepositoryAndReturnList() {
        // Given
        String playerId = "historyFan";
        List<GameHistory> mockHistory = List.of(
                GameHistory.builder().playerId(playerId).result(GameResult.WIN).build(),
                GameHistory.builder().playerId(playerId).result(GameResult.LOSS).build()
        );
        when(gameHistoryRepository.findByPlayerIdOrderByTimestampDesc(playerId)).thenReturn(mockHistory);

        // When
        List<GameHistory> result = gameHistoryService.getHistoryForPlayer(playerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(gameHistoryRepository).findByPlayerIdOrderByTimestampDesc(playerId);
    }
}