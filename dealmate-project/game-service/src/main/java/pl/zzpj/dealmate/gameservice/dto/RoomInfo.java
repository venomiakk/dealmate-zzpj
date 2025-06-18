package pl.zzpj.dealmate.gameservice.dto;

import pl.zzpj.dealmate.gameservice.model.EGameType;

import java.util.Set;

public record RoomInfo(
        String roomId,
        String joinCode,
        Set<String> players,
        String name,
        EGameType gameType,
        int maxPlayers,
        boolean isPublic,
        String ownerLogin,
        Long entryFee
) {
}
