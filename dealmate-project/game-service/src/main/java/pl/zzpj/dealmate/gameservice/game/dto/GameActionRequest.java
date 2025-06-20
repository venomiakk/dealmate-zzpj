package pl.zzpj.dealmate.gameservice.game.dto;

public record GameActionRequest(
        // Zawiera naszą dotychczasową akcję, np. { "action": "HIT" }
        PlayerAction action,
        // Dodajemy pole z loginem gracza
        String playerId
) {
}