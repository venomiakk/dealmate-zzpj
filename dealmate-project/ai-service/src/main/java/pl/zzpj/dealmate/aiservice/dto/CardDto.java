package pl.zzpj.dealmate.aiservice.dto;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;

public record CardDto(
        @NotNull Rank rank,
        @NotNull Suit suit
) {}
