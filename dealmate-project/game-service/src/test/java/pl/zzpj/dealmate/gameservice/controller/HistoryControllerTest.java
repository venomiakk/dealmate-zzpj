package pl.zzpj.dealmate.gameservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
// Uwaga: SecurityConfig musi być wyłączony lub poprawnie skonfigurowany, aby te testy działały.
// Załóżmy, że endpoint /history/** jest publicznie dostępny lub testujemy z mockowym użytkownikiem.
@WithMockUser
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameHistoryService gameHistoryService;

    @Test
    void getPlayerHistory_shouldReturnHistoryForPlayer() throws Exception {
        // Given
        String playerId = "playerWithHistory";
        GameHistory historyEntry = GameHistory.builder()
                .id(1L)
                .playerId(playerId)
                .gameType(EGameType.BLACKJACK)
                .result(GameResult.WIN)
                .amount(new BigDecimal("100"))
                .timestamp(LocalDateTime.now())
                .build();

        when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(List.of(historyEntry));

        // When & Then
        mockMvc.perform(get("/history/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].gameType", is("BLACKJACK")))
                .andExpect(jsonPath("$[0].result", is("WIN")))
                .andExpect(jsonPath("$[0].amount", is(100)));
    }

    @Test
    void getPlayerHistory_shouldReturnEmptyListForPlayerWithNoHistory() throws Exception {
        // Given
        String playerId = "newPlayer";
        when(gameHistoryService.getHistoryForPlayer(playerId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/history/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
