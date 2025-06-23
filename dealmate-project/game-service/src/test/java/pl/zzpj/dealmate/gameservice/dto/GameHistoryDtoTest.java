package pl.zzpj.dealmate.gameservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GameHistoryDtoTest {

    @Test
    void shouldCreateGameHistoryDtoCorrectly() {

        Long id = 1L;
        String gameType = "BLACKJACK";
        String result = "WIN";
        BigDecimal amount = new BigDecimal("100.50");
        LocalDateTime timestamp = LocalDateTime.now();


        GameHistoryDto dto = new GameHistoryDto(id, gameType, result, amount, timestamp);


        assertEquals(id, dto.id());
        assertEquals(gameType, dto.gameType());
        assertEquals(result, dto.result());
        assertEquals(amount, dto.amount());
        assertEquals(timestamp, dto.timestamp());
    }
}
