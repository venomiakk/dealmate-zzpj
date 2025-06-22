package pl.zzpj.dealmate.gameservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.zzpj.dealmate.gameservice.dto.GameHistoryDto;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HistoryControllerTest {

    private GameHistoryService gameHistoryService;
    private HistoryController historyController;

    @BeforeEach
    void setup() {
        gameHistoryService = mock(GameHistoryService.class);
        historyController = new HistoryController(gameHistoryService);
    }

    @Test
    void shouldReturnPlayerHistory() {
        GameHistory history = GameHistory.builder()
                .id(1L)
                .playerId("player1")
                .gameType(EGameType.BLACKJACK)
                .result(GameResult.WIN)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .build();

        when(gameHistoryService.getHistoryForPlayer("player1"))
                .thenReturn(Collections.singletonList(history));

        List<GameHistoryDto> result = historyController.getPlayerHistory("player1").getBody();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).gameType()).isEqualTo("BLACKJACK");
    }
}
