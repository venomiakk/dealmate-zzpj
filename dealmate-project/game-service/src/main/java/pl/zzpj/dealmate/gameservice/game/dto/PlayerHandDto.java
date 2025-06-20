package pl.zzpj.dealmate.gameservice.game.dto;

import java.util.List;

public record PlayerHandDto(String playerId, List<CardDto> cards, int value, String status) {}