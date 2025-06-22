package pl.zzpj.dealmate.gameservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.zzpj.dealmate.gameservice.dto.GameHistoryDto;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;
import pl.zzpj.dealmate.gameservice.service.GameHistoryGraphService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebMvcTest(HistoryController.class)
@WithMockUser
class HistoryControllerTest {

    private GameHistoryService gameHistoryService;
    private HistoryController historyController;

    @BeforeEach
    void setup() {
        gameHistoryService = mock(GameHistoryService.class);
        historyController = new HistoryController(gameHistoryService);
    }

    @MockitoBean
    private GameHistoryGraphService gameHistoryGraphService;

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

    @Test
    void generateGraphFromJson_shouldReturnGraphString() throws Exception {
        // Given
        String playerId = "player1";
        String expectedGraph = "graph-data";
        when(gameHistoryGraphService.generateGraphFromJson(playerId)).thenReturn(expectedGraph);

        // When & Then
        mockMvc.perform(get("/history/generateGraph/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedGraph));
    }

    @Test
    void generateGraphFromJson_shouldReturnInternalServerErrorOnException() throws Exception {
        // Given
        String playerId = "playerWithError";
        when(gameHistoryGraphService.generateGraphFromJson(playerId))
                .thenThrow(new RuntimeException("Błąd generowania wykresu"));

        // When & Then
        mockMvc.perform(get("/history/generateGraph/{playerId}", playerId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Błąd: Błąd generowania wykresu")));
    }
}