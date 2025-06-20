package pl.zzpj.dealmate.aiservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record PokerAiRequest(
        @NotEmpty int dealer,
        @NotEmpty int hand

) {}