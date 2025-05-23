package pl.zzpj.dealmate.aiservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record PokerAiRequest(
        @NotEmpty List<CardDto> deck,
        List<CardDto> discardPile,
        @NotEmpty List<CardDto> hand,
        List<CardDto> tableCards,

        @NotNull PokerGameType gameType
) {}