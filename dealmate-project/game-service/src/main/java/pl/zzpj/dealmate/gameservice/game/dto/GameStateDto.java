package pl.zzpj.dealmate.gameservice.game.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record GameStateDto(
        String gameStatus,
        Map<String, PlayerHandDto> playerHands,
        PlayerHandDto dealerHand,
        String currentPlayerId,
        BigDecimal pot,
        List<String> winners,
        String message
) {}