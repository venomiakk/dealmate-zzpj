package pl.zzpj.dealmate.gameservice.dto;

import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GameHistoryDto(
        Long id,
        String gameType,
        String result,
        BigDecimal amount,
        LocalDateTime timestamp
) {
}